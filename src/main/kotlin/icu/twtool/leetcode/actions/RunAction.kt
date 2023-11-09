package icu.twtool.leetcode.actions

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.getUserData
import com.intellij.openapi.ui.putUserData
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiUtilCore
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.api.enumerate.CheckState
import icu.twtool.leetcode.api.model.ConsolePanelConfig
import icu.twtool.leetcode.api.model.InterpretSolution
import icu.twtool.leetcode.api.model.check
import icu.twtool.leetcode.api.model.interpretSolution
import icu.twtool.leetcode.editor.LeetCodeFileBinaryData
import icu.twtool.leetcode.services.LeetCodeRequestService
import icu.twtool.leetcode.ui.dialogs.TestResultDialog
import icu.twtool.leetcode.util.application
import icu.twtool.leetcode.util.createCoroutinesScope
import icu.twtool.leetcode.util.errorNotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString

class RunAction : AnAction(), Disposable {

    private val log = thisLogger()

    companion object {

        val QUESTION_DATA_KEY = Key.create<LeetCodeFileBinaryData>("LeetCodeFileBinaryDataKey")

        val QUESTION_SOLUTION_FILE_KEY = Key.create<VirtualFile>("LeetCodeSolutionFileKey")

        val RUNNING = Key.create<Boolean>("LeetCodeSolutionRunning")
    }

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val mutex = Mutex()

    private val requestService by lazy { LeetCodeRequestService.getInstance() }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val targetComponent = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Splitter ?: return

        if (targetComponent.getUserData(RUNNING) == true) return

        val data = targetComponent.getUserData(QUESTION_DATA_KEY) ?: return
        val virtualFile = targetComponent.getUserData(QUESTION_SOLUTION_FILE_KEY) ?: return

        val consolePanelConfig = data.consolePanelConfig ?: return

        val metaData = requestService.api.json.decodeFromString<ConsolePanelConfig.MetaData>(consolePanelConfig.metaData)

        val psiFile = PsiUtilCore.getPsiFile(project, virtualFile)

        val typedCode = psiFile.children.find {
            val psiClass = it as? PsiClass ?: return@find false
            psiClass.name == "Solution"
        }?.text ?: return

        scope.launch {
            mutex.withLock {
                if (targetComponent.getUserData(RUNNING) == true) return@launch
                targetComponent.putUserData(RUNNING, true)
            }

            ProgressManager.getInstance().run(object: Task.Backgroundable(project, "正在运行", false) {
                override fun run(indicator: ProgressIndicator) {
                    val result: Boolean = runBlocking {
                        val titleSlug = data.question.titleSlug
                        val dataInput = consolePanelConfig.exampleTestcases
                        val questionId = consolePanelConfig.questionId
                        val interpretSolution = requestService.api.interpretSolution(
                            titleSlug, InterpretSolution.Param(
                                dataInput = dataInput,
                                lang = "java",
                                questionId = questionId,
                                typedCode = typedCode
                            )
                        ) ?: return@runBlocking false

                        try {
                            while (true) {
                                val check = requestService.api.check(interpretSolution.interpretId)
                                    ?: return@runBlocking false

                                if (check.state != CheckState.SUCCESS) {
                                    delay(1000)
                                    continue
                                }

                                log.info("check = \n ${requestService.api.json.encodeToString(check)}")

                                application.invokeLater {
                                    TestResultDialog(project, check, metaData, interpretSolution.testCase).show()
                                }

                                break
                            }
                        } catch (e: Exception) {
                            log.error(e)
                            return@runBlocking false
                        }

                        true
                    }

                    if (!result) {
                        errorNotify(project, MyBundle.message("run.failure"))
                    }

                    targetComponent.putUserData(RUNNING, false)
                }
            })
        }
    }

    override fun update(e: AnActionEvent) {
        val targetComponent = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Splitter ?: return
        e.presentation.isEnabled = targetComponent.getUserData(RUNNING) != true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun dispose() {
        scope.cancel()
    }
}