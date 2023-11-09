package icu.twtool.leetcode.ui.window

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.io.write
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.constants.PluginConstants
import icu.twtool.leetcode.editor.LeetCodeFileType
import icu.twtool.leetcode.editor.initData
import icu.twtool.leetcode.listeners.notifier.QuestionListUpdateNotifier
import icu.twtool.leetcode.listeners.notifier.ToggleActionStateChangeNotifier
import icu.twtool.leetcode.listeners.notifier.UserStatusChangeNotifier
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.services.LeetCodeStatusService
import icu.twtool.leetcode.ui.component.QuestionListComponent
import icu.twtool.leetcode.ui.component.UserInfoAndQuestionInfoPanel
import icu.twtool.leetcode.util.createCoroutinesScope
import icu.twtool.leetcode.util.messageBusConnect
import icu.twtool.leetcode.util.pluginRootPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

class LeetCodeToolWindowImpl(
    private val project: Project,
    private val toolWindow: ToolWindow
) : SimpleToolWindowPanel(true, false),
    UserStatusChangeNotifier, QuestionListUpdateNotifier, ToggleActionStateChangeNotifier, Disposable {

    private val log = thisLogger()

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val questionListService = LeetCodeQuestionListService.getInstance()
    private val statusService = LeetCodeStatusService.getInstance()
    private val messageBusConnect: MessageBusConnection = messageBusConnect()

    private val contentPanel = BorderLayoutPanel()

    private val searchTextField: JBTextField = JBTextField().apply {
        emptyText.setText(MyBundle.message("text.field.empty.text.question.search"))
        isVisible = false

        var job: Job? = null
        val search = { text: String, wait: Boolean ->
            job?.cancel()
            job = scope.launch {
                if (wait) delay(2000)
                this.ensureActive()
                questionListService.filters.searchKeywords = text.trim()
                questionListService.fetchData(0)
            }
        }
        document.addDocumentListener(object: DocumentListener {
            override fun insertUpdate(p0: DocumentEvent?) {
                search(text, true)
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                search(text, true)
            }

            override fun changedUpdate(p0: DocumentEvent?) {
                search(text, true)
            }
        })
        addActionListener {
            search(text, false)
        }
    }

    private val userInfoAndQuestionInfoPanel: UserInfoAndQuestionInfoPanel

    private val mouseAdapter: MouseAdapter = object : MouseAdapter() {

        override fun mouseClicked(e: MouseEvent?) {
            if (e?.clickCount != 2) return
            val index = questionList.locationToIndex(e.point)
            val model = questionListService.dataModel

            if (index < 0 || index >= model.size) return

            val question = model.getElementAt(index)
            val id = question.frontendQuestionId
            val titleSlug = question.titleSlug
            val titleCn = question.titleCn
            val extensions = LeetCodeFileType.INSTANCE.defaultExtension

            val path = pluginRootPath.resolve("${id}.${titleSlug}/${titleCn}.${extensions}").run {
                if (exists()) this
                else {
                    parent.createDirectories()
                    val new = createFile()
                    new.initData(question)
                    new
                }
            }

            log.info("path = $path")

            val vf = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path) ?: return

            FileEditorManager.getInstance(project).openFile(vf, true, true)
        }
    }
    private val questionList = QuestionListComponent(mouseAdapter, questionListService.dataModel)

    init {
        name = "LeetCode Tool Window"
        messageBusConnect.subscribe(UserStatusChangeNotifier.TOPIC, this)
        messageBusConnect.subscribe(QuestionListUpdateNotifier.TOPIC, this)
        messageBusConnect.subscribe(ToggleActionStateChangeNotifier.TOPIC, this)

        val actionManager = ActionManager.getInstance()
        val actionGroup = actionManager.getAction(PluginConstants.TOOL_WINDOW_GROUP_ACTION_ID) as ActionGroup
        val actionToolbar = actionManager.createActionToolbar(PluginConstants.TOOL_WINDOW_ID, actionGroup, true)
        actionToolbar.targetComponent = contentPanel
        toolbar = actionToolbar.component

        val userStatus = statusService.get()

        userInfoAndQuestionInfoPanel = UserInfoAndQuestionInfoPanel(
            userStatus?.username,
            questionListService.skip, questionListService.limit, questionListService.total
        )

        contentPanel.add(searchTextField, BorderLayout.NORTH)
        contentPanel.add(userInfoAndQuestionInfoPanel, BorderLayout.SOUTH)
        contentPanel.add(JBScrollPane(questionList), BorderLayout.CENTER)

        setContent(contentPanel)
        scope.launch {
            questionListService.fetchData()
        }
    }

    override fun updateQuestionListState(result: Boolean?) {
        log.info("update question list state : $result")
        questionList.setPaintBusy(questionListService.loading)
        if (result == true) {
            updateUserInfoAndQuestionInfo()
        }
    }

    override fun userStatusChanged() {
        updateUserInfoAndQuestionInfo()
    }

    override fun searchStateChange(state: Boolean) {
        searchTextField.isVisible = state
    }

    private fun updateUserInfoAndQuestionInfo() {
        val userStatus = statusService.get()
        userInfoAndQuestionInfoPanel.update(
            userStatus?.username,
            questionListService.skip, questionListService.limit, questionListService.total
        )
    }

    override fun dispose() {
        log.info("dispose...")
        messageBusConnect.dispose()
        scope.cancel()
        questionList.dispose()
    }
}