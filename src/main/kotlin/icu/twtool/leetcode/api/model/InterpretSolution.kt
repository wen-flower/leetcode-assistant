package icu.twtool.leetcode.api.model

import icu.twtool.leetcode.api.LeetCodeApi
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterpretSolution(
    @SerialName("interpret_expected_id")
    val interpretExpectedId: String,
    @SerialName("interpret_id")
    val interpretId: String,
    @SerialName("test_case")
    val testCase: String
) {

    @Serializable
    data class Param(
        @SerialName("data_input")
        val dataInput: String,
        val lang: String,
        @SerialName("question_id")
        val questionId: String,
        @SerialName("typed_code")
        val typedCode: String
    )
}

suspend fun LeetCodeApi.interpretSolution(titleSlug: String, param: InterpretSolution.Param): InterpretSolution? {
    return post("problems/$titleSlug/interpret_solution/", param) {
        set(HttpHeaders.Referrer, "https://leetcode.cn/problems/two-sum/")
    }
}

