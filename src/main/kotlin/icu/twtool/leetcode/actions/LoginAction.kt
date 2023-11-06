package icu.twtool.leetcode.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.twtool.leetcode.services.LeetCodeStatusService
import icu.twtool.leetcode.ui.dialogs.LoginDialog

class LoginAction : AnAction() {

    private val statusService = LeetCodeStatusService.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        LoginDialog(e.project).show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = statusService.get() == null
        e.presentation.isEnabled = statusService.initialized
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}