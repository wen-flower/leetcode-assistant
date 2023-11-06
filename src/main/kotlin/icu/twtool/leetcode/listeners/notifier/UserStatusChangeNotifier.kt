package icu.twtool.leetcode.listeners.notifier

import com.intellij.util.messages.Topic
import icu.twtool.leetcode.constants.PluginConstants

/**
 * 用户状态改变通知
 */
interface UserStatusChangeNotifier {

    companion object {

        private const val TOPIC_ID = "${PluginConstants.ID}.user.status.change.topic"

        @Topic.AppLevel
        val TOPIC: Topic<UserStatusChangeNotifier> = Topic.create(TOPIC_ID, UserStatusChangeNotifier::class.java)
    }

    fun userStatusChanged()
}