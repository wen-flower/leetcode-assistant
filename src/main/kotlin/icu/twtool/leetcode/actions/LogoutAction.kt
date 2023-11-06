package icu.twtool.leetcode.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.constants.PluginConstants.LEET_CODE_HOST
import icu.twtool.leetcode.listeners.notifier.LogoutNotifier
import icu.twtool.leetcode.services.LeetCodeStatusService
import icu.twtool.leetcode.ui.dialogs.ConfirmDialog
import icu.twtool.leetcode.util.createBrowser
import icu.twtool.leetcode.util.infoNotify
import icu.twtool.leetcode.util.syncPublisher

class LogoutAction : AnAction() {

    private val statusService = LeetCodeStatusService.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        ConfirmDialog(e.project, MyBundle.message("confirm.logout")) {
            createBrowser().apply {
                jbCefCookieManager.deleteCookies(LEET_CODE_HOST, "").get()
                dispose()
                LogoutNotifier.TOPIC.syncPublisher().logout()
                infoNotify(e.project, MyBundle.message("logout.success"))
            }
        }.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = statusService.get() != null && statusService.initialized
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}