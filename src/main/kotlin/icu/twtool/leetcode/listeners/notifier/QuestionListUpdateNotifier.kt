package icu.twtool.leetcode.listeners.notifier

import com.intellij.util.messages.Topic
import icu.twtool.leetcode.constants.PluginConstants

interface QuestionListUpdateNotifier {

    companion object {

        private const val TOPIC_ID = "${PluginConstants.ID}.question.list.update.topic"

        @Topic.ProjectLevel
        val TOPIC: Topic<QuestionListUpdateNotifier> = Topic.create(TOPIC_ID, QuestionListUpdateNotifier::class.java)
    }

    fun updateQuestionListState(result: Boolean? = null)
}