package icu.twtool.leetcode.ui.component.editor

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.ui.dialogs.ConfirmDialog
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton

class FileDamagePanel(private val project: Project, private val file: VirtualFile) :
    JBPanel<JBPanel<*>>(GridBagLayout()) {

    private val damageLabel = JBLabel(MyBundle.message("file.damage"))
    private val deleteFileButton = JButton(MyBundle.getDeleteButtonText()).apply {
        putClientProperty("JButton.backgroundColor", JBColor.namedColor("Button.default.startBackground"))

        addActionListener {
            ConfirmDialog(project, MyBundle.message("confirm.delete", file.nameWithoutExtension)) {
                runWriteAction {
                    file.delete(project)
                }
            }.show()
        }
    }

    init {
        val constraints = GridBagConstraints().apply {
            insets = JBUI.insets(8)
            gridx = 0
            gridy = 0
            anchor = GridBagConstraints.CENTER
        }

        add(damageLabel, constraints)

        constraints.gridy += 1
        add(deleteFileButton, constraints)
    }
}