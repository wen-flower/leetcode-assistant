package icu.twtool.leetcode.api.model

import icu.twtool.leetcode.api.LeetCodeApi
import icu.twtool.leetcode.api.enumerate.CheckState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Check(

    @SerialName("code_answer")
    val codeAnswer: List<String>? = null,
//    val code_output: List<String>?,
    @SerialName("compare_result")
    val compareResult: String? = null,
    @SerialName("correct_answer")
    val correctAnswer: Boolean? = null, // 111
    @SerialName("display_runtime")
    val displayRuntime: String? = null, // 0
    @SerialName("elapsed_time")
    val elapsedTime: Int? = null,
    @SerialName("expected_code_answer")
    val expectedCodeAnswer: List<String>? = null,
//    val expected_code_output: List<String>,
    @SerialName("expected_elapsed_time")
    val expectedElapsedTime: Int? = null,
    @SerialName("expected_lang")
    val expectedLang: String? = null,
    @SerialName("expected_memory")
    val expectedMemory: Int? = null,
    @SerialName("expected_run_success")
    val expectedRunSuccess: Boolean? = null,
    @SerialName("expected_status_code")
    val expectedStatusCode: Int? = null,
    @SerialName("expected_status_runtime")
    val expectedStatusRuntime: String? = null,
    @SerialName("expected_std_output_list")
    val expectedStdOutputList: List<String>? = null,
    @SerialName("expected_task_finish_time")
    val expectedTaskFinishTime: Long? = null,
    @SerialName("compile_error")
    val compileError: String? = null,
    @SerialName("fast_submit")
    val fastSubmit: Boolean? = null,
    @SerialName("full_runtime_error")
    val fullRuntimeError: String? = null,
    @SerialName("full_compile_error")
    val fullCompileError: String? = null,
    @SerialName("lang")
    val lang: String? = null,
    @SerialName("memory")
    val memory: Int? = null,
//    val memory_percentile: Double?,
    @SerialName("pretty_lang")
    val prettyLang: String? = null,
    @SerialName("run_success")
    val runSuccess: Boolean? = null,
//    val runtime_percentile: Double?,
    @SerialName("runtime_error")
    val runtimeError: String? = null,
    @SerialName("state")
    val state: CheckState,
    @SerialName("status_code")
    val statusCode: Int? = null,
    @SerialName("status_memory")
    val statusMemory: String? = null,
    @SerialName("status_msg")
    val statusMsg: String? = null,
    @SerialName("status_runtime")
    val statusRuntime: String? = null,
    @SerialName("std_output_list")
    val stdOutputList: List<String>? = null,
    @SerialName("submission_id")
    val submissionId: String? = null,
    @SerialName("task_finish_time")
    val taskFinishTime: Long? = null,
    @SerialName("task_name")
    val taskName: String? = null,
    @SerialName("total_correct")
    val totalCorrect: Int? = null,
    @SerialName("total_testcases")
    val totalTestcases: Int? = null,
) {

    companion object {

        const val AC_CODE = 10 // 执行完成（包含答案错误）

        const val COMPILER_ERROR_CODE = 20 // 编译错误
        const val RUNTIME_ERROR_CODE = 15 // 执行错误


    }
}

suspend fun LeetCodeApi.check(runCode: String): Check? {
    return get("submissions/detail/$runCode/check/")
}