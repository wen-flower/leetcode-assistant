package icu.twtool.leetcode.actions

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.util.application
import icu.twtool.leetcode.util.createCoroutinesScope
import icu.twtool.leetcode.util.errorNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoadRefreshPageAction : AnAction(), Disposable {

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val questionListService by lazy { LeetCodeQuestionListService.getInstance() }

    override fun actionPerformed(e: AnActionEvent) {
        scope.launch {
            val result = questionListService.refresh()
            if (!result) {
                application.invokeLater {
                    errorNotify(e.project, MyBundle.message("loading.failure"))
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = questionListService.loading.not()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun dispose() {
        scope.cancel()
    }
}