package icu.twtool.leetcode.api

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.constants.MyRegistry
import icu.twtool.leetcode.constants.PluginConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.modules.SerializersModule
import java.net.http.HttpClient.Version

/**
 * LeetCode 客户端
 * @author wen
 * @since 2023-11-03
 */
class LeetCodeApi(cookiesStorage: CookiesStorage) : Disposable {

    private val log = thisLogger()

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true

        serializersModule = SerializersModule {
//            contextual()
        }
    }

    val client = HttpClient(Java) {
        engine {
            pipelining = true
            protocolVersion = Version.HTTP_2
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    log.info(message)
                }
            }
            filter {
                log.info("debug: ${MyRegistry.debug}")
                MyRegistry.debug
            }
            level = LogLevel.ALL
        }

        install(HttpCookies) {
            storage = cookiesStorage
        }
    }

    suspend inline fun <reified T> graphql(
        operationName: String,
        query: String,
        path: String = "graphql",
        crossinline variables: JsonObjectBuilder.() -> Unit = {}
    ): T? {
        val response = client.post {
            defaultUrl(path)
            defaultHeaders()
            setJsonBody(buildJsonObject {
                put("operationName", operationName)
                put("query", query)
                putJsonObject("variables") {
                    variables()
                }
            })
        }

        return if (response.status == HttpStatusCode.OK) response.body<T>()
        else null
    }

    /**
     * 设置 JSON 请求 body
     */
    inline fun <reified T> HttpRequestBuilder.setJsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    /**
     * 设置默认 url
     */
    fun HttpRequestBuilder.defaultUrl(path: String, block: URLBuilder.() -> Unit = {}) = url {
        protocol = URLProtocol.HTTPS
        host = "leetcode.cn"
        path(path)
        block()
    }

    /**
     * 设置默认请求头
     */
    fun HttpRequestBuilder.defaultHeaders(block: HeadersBuilder.() -> Unit = {}) {
        headers {
            set(HttpHeaders.UserAgent, PluginConstants.USER_AGENT)
            block()
        }
    }

    override fun dispose() {
        log.info("dispose...")
        client.close()
    }
}