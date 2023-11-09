package icu.twtool.leetcode.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.getUserData
import icu.twtool.leetcode.api.model.ConsolePanelConfig
import icu.twtool.leetcode.services.LeetCodeRequestService
import icu.twtool.leetcode.ui.dialogs.TestResultDialog
import kotlinx.serialization.json.Json

class SubmitAction : AnAction() {

    private val log = thisLogger()

    private val requestService by lazy { LeetCodeRequestService.getInstance() }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val targetComponent = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Splitter ?: return

        val data = targetComponent.getUserData(RunAction.QUESTION_DATA_KEY) ?: return

        val metaData = requestService.api.json.decodeFromString<ConsolePanelConfig.MetaData>((data.consolePanelConfig ?: return).metaData)

        log.info("metaData = $metaData")

        TestResultDialog(
            project,
            requestService.api.json.decodeFromString("""
{
    "status_code": 15,
    "lang": "java",
    "run_success": false,
    "runtime_error": "Line 3: java.lang.ArithmeticException: / by zero",
    "full_runtime_error": "java.lang.ArithmeticException: / by zero\n  at line 3, Solution.twoSum\n  at line 54, __DriverSolution__.__helper__\n  at line 87, __Driver__.main",
    "status_runtime": "N/A",
    "memory": 39068000,
    "code_answer": [],
    "code_output": [],
    "std_output_list": [
        ""
    ],
    "elapsed_time": 170,
    "task_finish_time": 1699517133946,
    "task_name": "judger.runcodetask.RunCode",
    "status_msg": "Runtime Error",
    "state": "SUCCESS",
    "fast_submit": false,
    "total_correct": null,
    "total_testcases": null,
    "submission_id": "runcode_1699517132.8683045_mcniUlyNZa",
    "runtime_percentile": null,
    "status_memory": "N/A",
    "memory_percentile": null,
    "pretty_lang": "Java"
}
            """.trimIndent()),
            metaData,
            "[2,7,11,15]\n" +
                    "9\n" +
                    "[3,2,4]\n" +
                    "6\n" +
                    "[3,3]\n" +
                    "6"
        ).show()
    }

    override fun update(e: AnActionEvent) {
        val targetComponent = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Splitter ?: return
        e.presentation.isEnabled = targetComponent.getUserData(RunAction.RUNNING) != true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}