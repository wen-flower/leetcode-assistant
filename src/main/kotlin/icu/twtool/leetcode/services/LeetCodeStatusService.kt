package icu.twtool.leetcode.services

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.api.model.UserStatus
import icu.twtool.leetcode.api.model.getUserStatus
import icu.twtool.leetcode.listeners.notifier.LogoutNotifier
import icu.twtool.leetcode.listeners.notifier.UserStatusChangeNotifier
import icu.twtool.leetcode.util.application
import icu.twtool.leetcode.util.errorNotify
import icu.twtool.leetcode.util.getService
import icu.twtool.leetcode.util.infoNotify
import icu.twtool.leetcode.util.messageBusConnect
import icu.twtool.leetcode.util.syncPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Service(Service.Level.APP)
class LeetCodeStatusService : Disposable {

    companion object {

        fun getInstance(): LeetCodeStatusService = application.getService()
    }

    private val log = thisLogger()

    private val messageBusConnect = messageBusConnect().apply {
        subscribe(LogoutNotifier.TOPIC, object : LogoutNotifier {
            override fun logout() {
                userStatus = null
            }
        })
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    var initialized = false
        private set

    private var userStatus: UserStatus? = null
        set(value) {
            field = value?.takeIf { it.isSignedIn }
            UserStatusChangeNotifier.TOPIC.syncPublisher().userStatusChanged()
            ActivityTracker.getInstance().inc()
        }

    init {
        scope.launch {
            val userStatus = LeetCodeRequestService.getInstance().api.getUserStatus()
            initialized = true
            this@LeetCodeStatusService.userStatus = userStatus
        }
    }

    fun get(): UserStatus? {
        return userStatus
    }

    fun login(project: Project?, userStatus: UserStatus?): Boolean {
        if (userStatus?.isSignedIn == true) {
            this.userStatus = userStatus
            infoNotify(project, MyBundle.message("login.success"))
            return true
        }
        this.userStatus = null
        errorNotify(project, MyBundle.message("login.failure"))
        return false
    }

    override fun dispose() {
        scope.cancel()
        messageBusConnect.dispose()
    }
}