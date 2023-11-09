package icu.twtool.leetcode.editor

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.putUserData
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.actions.RunAction
import icu.twtool.leetcode.api.model.getConsolePanelConfig
import icu.twtool.leetcode.api.model.getQuestionEditorData
import icu.twtool.leetcode.api.model.getQuestionTranslations
import icu.twtool.leetcode.constants.PluginConstants
import icu.twtool.leetcode.services.LeetCodeRequestService
import icu.twtool.leetcode.ui.component.editor.FileDamagePanel
import icu.twtool.leetcode.ui.component.editor.QuestionInfoComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.beans.PropertyChangeListener
import java.nio.file.StandardOpenOption
import javax.swing.JComponent
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class LeetCodeFileEditor(
    private val project: Project,
    private val vf: VirtualFile,
    private val scope: CoroutineScope
) : UserDataHolderBase(), FileEditor {

    companion object {
        private const val NAME = "LeetCodeFileEditor"
    }

    private val log = thisLogger()

    private val requestService by lazy { LeetCodeRequestService.getInstance() }

    private val mutex = Mutex()
    private val loading: Boolean get() = mutex.isLocked
    private var verified: Boolean? = null

    private val splitter = Splitter()

    private val toolbar: ActionToolbar by lazy {
        val actionManager = ActionManager.getInstance()
        val actionGroup = actionManager.getAction(PluginConstants.EDITOR_GROUP_ACTION_ID) as ActionGroup
        val toolbar = actionManager.createActionToolbar(
            ActionPlaces.EDITOR_TOOLBAR, actionGroup, true
        )
        toolbar.targetComponent = splitter
        toolbar
    }

    private val editorComponent = BorderLayoutPanel().apply {
        background = UIUtil.getEditorPaneBackground()
        addToTop(toolbar.component)
    }
    private var editorCenterComponent: JComponent? = null
        set(value) {
            if (field == value) return
            if (field != null) editorComponent.remove(field)
            if (value != null) editorComponent.addToCenter(value)
            field = value
        }

    private val loadingComponent = AsyncProcessIcon.BigCentered("loading")

    private val questionInfoComponent: QuestionInfoComponent by lazy { QuestionInfoComponent() }
    private var textEditor: FileEditor? = null

    init {
        updateEditorComponent(null)
        scope.launch {
            val data = mutex.withLock {
                val data = file.readData()
                verified = data != null
                data
            }
            updateEditorComponent(data)
            fetchData()
        }
    }

    private suspend fun updateTextEditor(data: LeetCodeFileBinaryData) {
        splitter.putUserData(RunAction.QUESTION_DATA_KEY, data)
        val solutionPath = vf.toNioPath().parent.resolve("Solution.java")
        if (solutionPath.notExists()) {
            solutionPath.createFile()
        }
        if (solutionPath.readText().isBlank()) {
            data.editorData?.codeSnippets?.find { it.langSlug == "java" }?.code?.let {
                solutionPath.writeText(it, Charsets.UTF_8, StandardOpenOption.WRITE)
            }
        }
        val solutionFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(solutionPath)!!
        splitter.putUserData(RunAction.QUESTION_SOLUTION_FILE_KEY, solutionFile)
        writeAction {
            textEditor = TextEditorProvider.getInstance().createEditor(project, solutionFile)
            if (splitter.secondComponent != textEditor) {
                splitter.secondComponent = textEditor?.component
            }
        }
    }

    private fun fetchData(refresh: Boolean = false) {
        log.info("fetchData($refresh)...")
        if (loading || verified != true) return
        scope.launch {
            val data = file.readData() ?: return@launch
            if (data.initialized) {
                updateTextEditor(data)
                return@launch
            }
            mutex.withLock {
                log.info("loading data...")
                val titleSlug = data.question.titleSlug
                val editorDataDeferred = async {
                    requestService.api.getQuestionEditorData(titleSlug)
                }
                val translationsDeferred = async {
                    requestService.api.getQuestionTranslations(titleSlug)
                }
                val consolePanelConfigDeferred = async {
                    requestService.api.getConsolePanelConfig(titleSlug)
                }
                data.editorData = editorDataDeferred.await()
                data.translations = translationsDeferred.await()
                data.consolePanelConfig = consolePanelConfigDeferred.await()
                data.initialized = true
                file.writeData(data)
                updateTextEditor(data)
            }
            updateEditorComponent(data)
        }
    }

    private fun updateEditorComponent(data: LeetCodeFileBinaryData?) {
        if (loading || verified == null) {
            editorCenterComponent = loadingComponent
            return
        }
        if (verified == false) {
            editorCenterComponent = FileDamagePanel(project, file)
            return
        }
        editorCenterComponent = splitter
        if (splitter.firstComponent == null) splitter.firstComponent = questionInfoComponent

        questionInfoComponent.updateInfo(data)
    }

    override fun dispose() {
        questionInfoComponent.dispose()
    }

    override fun getFile(): VirtualFile = vf

    override fun getComponent(): JComponent = editorComponent

    override fun getPreferredFocusedComponent(): JComponent? = null

    override fun getName(): String = NAME

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }
}