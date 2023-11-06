package icu.twtool.leetcode.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import icu.twtool.leetcode.api.LeetCodeApi
import icu.twtool.leetcode.listeners.notifier.LogoutNotifier
import icu.twtool.leetcode.util.getService
import icu.twtool.leetcode.util.messageBusConnect
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.hostIsIp
import io.ktor.http.isSecure
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader
import io.ktor.util.date.getTimeMillis
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

@Service(Service.Level.APP)
@State(
    name = "leetcode-request-service-state",
    storages = [Storage(StoragePathMacros.NON_ROAMABLE_FILE)]
)
class LeetCodeRequestService :
    SimplePersistentStateComponent<LeetCodeRequestService.State>(State()),
    CookiesStorage, Disposable {

    private val log = thisLogger()

    companion object {

        fun getInstance(): LeetCodeRequestService = application.getService()
    }

    private val messageBusConnect = messageBusConnect().apply {
        subscribe(LogoutNotifier.TOPIC, object : LogoutNotifier {
            override fun logout() {
                cookies = emptyList()
            }
        })
    }

    private val oldestCookie = AtomicLong(0)
    private val mutex = Mutex()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        val name = cookie.name
        if (name.isBlank()) return@withLock

        val cookies = cookies.toMutableList()
        cookies.removeAll { it.name == name && it.matches(requestUrl) }
        cookies.add(cookie)

        cookie.expires?.timestamp?.let {
            if (oldestCookie.get() > it) {
                oldestCookie.set(it)
            }
        }

        this.cookies = cookies
    }

    override fun close() {}

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = getTimeMillis()
        if (now >= oldestCookie.get()) cleanup(now)

        return@withLock cookies.filter { it.matches(requestUrl) }
    }

    private fun cleanup(timestamp: Long) {
        val cookies = cookies.toMutableList()
        cookies.removeAll { cookie ->
            val expires = cookie.expires?.timestamp ?: Long.MAX_VALUE
            expires < timestamp
        }

        val newOldest = cookies.fold(Long.MAX_VALUE) { acc, cookie ->
            cookie.expires?.timestamp?.let { min(acc, it) } ?: acc
        }

        oldestCookie.set(newOldest)

        this.cookies = cookies
    }

    val api = LeetCodeApi(this)

    override fun dispose() {
        log.info("dispose...")
        api.client.close()
        messageBusConnect.dispose()
    }

    private var cookies: List<Cookie>
        get() {
            return state.cookies.map { parseServerSetCookieHeader(it) }
        }
        set(value) {
            state.cookies = value.map { renderSetCookieHeader(it) }.toMutableList()
        }

    class State : BaseState() {

        var cookies by list<String>()
    }
}

private fun Cookie.matches(requestUrl: Url): Boolean {
    val domain = domain?.toLowerCasePreservingASCIIRules()?.trimStart('.')
        ?: error("Domain field should have the default value")

    val path = with(path) {
        val current = path ?: error("Path field should have the default value")
        if (current.endsWith('/')) current else "$path/"
    }

    val host = requestUrl.host.toLowerCasePreservingASCIIRules()
    val requestPath = let {
        val pathInRequest = requestUrl.encodedPath
        if (pathInRequest.endsWith('/')) pathInRequest else "$pathInRequest/"
    }

    if (host != domain && (hostIsIp(host) || !host.endsWith(".$domain"))) {
        return false
    }

    if (path != "/" &&
        requestPath != path &&
        !requestPath.startsWith(path)
    ) {
        return false
    }

    return !(secure && !requestUrl.protocol.isSecure())
}