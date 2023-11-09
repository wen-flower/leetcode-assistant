package icu.twtool.leetcode.ui.component

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.io.findOrCreateFile
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.utils.io.createFile
import com.intellij.ui.components.JBList
import com.intellij.util.io.write
import icu.twtool.leetcode.api.model.ProblemsetQuestionList.Question
import icu.twtool.leetcode.editor.LeetCodeFileType
import icu.twtool.leetcode.util.pluginRootPath
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

class QuestionListComponent(private val mouseAdapter: MouseAdapter, dataModel: QuestionListModel) : JBList<Question>(dataModel), Disposable {

    private val log = thisLogger()

    init {
        setCellRenderer { _, question, _, selected, hasFocus ->
            QuestionItemComponent(question, selected, hasFocus)
        }

        addMouseListener(mouseAdapter)
    }

    override fun dispose() {
        removeMouseListener(mouseAdapter)
    }
}