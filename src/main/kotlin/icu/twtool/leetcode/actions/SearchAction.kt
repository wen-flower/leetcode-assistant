package icu.twtool.leetcode.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import icu.twtool.leetcode.listeners.notifier.ToggleActionStateChangeNotifier
import icu.twtool.leetcode.util.syncPublisher

class SearchAction : ToggleAction() {

    private var selected = false

    override fun isSelected(e: AnActionEvent): Boolean = selected

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        selected = state
        ToggleActionStateChangeNotifier.TOPIC.syncPublisher().searchStateChange(state)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}