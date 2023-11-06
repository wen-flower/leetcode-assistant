package icu.twtool.leetcode.ui.dialogs

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.putUserData
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.util.application
import icu.twtool.leetcode.util.createCoroutinesScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JComponent
import kotlin.math.ceil

class InputPageDialog(project: Project?) : DialogWrapper(project, false) {

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val questionListService = LeetCodeQuestionListService.getInstance()

    private val pageField = JBTextField().apply {
        emptyText.text = MyBundle.message("please.input")
    }

    init {
        title = MyBundle.message("jump.page")
        init()
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return pageField
    }

    override fun doValidate(): ValidationInfo? {
        val page = pageField.text.trim().toIntOrNull()
        val maxPage = ceil(questionListService.total.toDouble() / questionListService.limit)
        if (page == null || page < 1 || page > maxPage) return ValidationInfo(MyBundle.message("please.input.page", maxPage), pageField)
        return null
    }

    override fun createCenterPanel(): JComponent {
        val centerPanel = BorderLayoutPanel()
        centerPanel.add(pageField, BorderLayout.CENTER)

        return centerPanel
    }

    override fun dispose() {
        super.dispose()
        scope.cancel()
    }

    override fun doOKAction() {
        if (!isOKActionEnabled) return
        isOKActionEnabled = false
        val page = pageField.text.trim().toInt() - 1
        scope.launch {
            questionListService.fetchData(page * questionListService.limit)
            application.invokeLater({
                thisLogger().info("================================================")
                close(OK_EXIT_CODE)
            }, ModalityState.any())
        }
    }
}