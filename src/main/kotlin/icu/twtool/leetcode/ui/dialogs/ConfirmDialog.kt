package icu.twtool.leetcode.ui.dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import icu.twtool.leetcode.MyBundle
import javax.swing.JComponent

class ConfirmDialog(project: Project?, private val tip: String, private val doAction: () -> Unit) : DialogWrapper(project, false) {

    init {
        title = MyBundle.message("confirm.dialog.title")
        setOKButtonText(MyBundle.message("button.confirm"))
        setCancelButtonText(MyBundle.message("button.cancel"))
        init()
    }

    override fun createCenterPanel(): JComponent = JBLabel(tip)

    override fun doOKAction() {
        super.doOKAction()
        doAction()
    }
}