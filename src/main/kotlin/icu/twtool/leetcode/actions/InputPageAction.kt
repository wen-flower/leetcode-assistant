package icu.twtool.leetcode.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.ui.dialogs.InputPageDialog

class InputPageAction : AnAction() {

    private val questionListService by lazy { LeetCodeQuestionListService.getInstance() }

    override fun actionPerformed(e: AnActionEvent) {
        InputPageDialog(e.project).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !questionListService.loading
        e.presentation.text = (questionListService.skip / questionListService.limit + 1).toString()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun displayTextInToolbar(): Boolean {
        return true
    }
}