package icu.twtool.leetcode.listeners.notifier

import com.intellij.util.messages.Topic
import icu.twtool.leetcode.constants.PluginConstants

interface LogoutNotifier {

    companion object {

        private const val TOPIC_ID = "${PluginConstants.ID}.logout.topic"

        @Topic.AppLevel
        val TOPIC = Topic.create(TOPIC_ID, LogoutNotifier::class.java)
    }

    fun logout()
}