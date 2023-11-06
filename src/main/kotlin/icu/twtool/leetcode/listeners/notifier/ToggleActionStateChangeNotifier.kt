package icu.twtool.leetcode.listeners.notifier

import com.intellij.util.messages.Topic
import icu.twtool.leetcode.constants.PluginConstants

interface ToggleActionStateChangeNotifier{

    companion object {

        private const val TOPIC_ID = "${PluginConstants.ID}.toggle.action.state.change.topic"

        @Topic.ProjectLevel
        val TOPIC = Topic.create(TOPIC_ID, ToggleActionStateChangeNotifier::class.java)
    }

    fun searchStateChange(state: Boolean) {}
}