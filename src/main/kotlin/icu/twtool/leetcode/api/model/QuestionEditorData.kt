package icu.twtool.leetcode.api.model

import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.api.LeetCodeApi
import icu.twtool.leetcode.api.getObj
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put

@Serializable
data class QuestionEditorData(
    val enableRunCode: Boolean,
    val envInfo: String,
    val frontendPreviews: String,
    val hasFrontendPreview: Boolean,
    val questionFrontendId: String,
    val questionId: String,
    val codeSnippets: List<CodeSnippet>
) : java.io.Serializable {

    @Serializable
    data class CodeSnippet(
        val code: String,
        val lang: String,
        val langSlug: String
    ) : java.io.Serializable
}

suspend fun LeetCodeApi.getQuestionEditorData(titleSlug: String): QuestionEditorData? {
    val obj = graphql<JsonObject>(
        "questionEditorData",
        "query questionEditorData(\$titleSlug: String!) {\n  question(titleSlug: \$titleSlug) {\n    questionId\n    questionFrontendId\n    codeSnippets {\n      lang\n      langSlug\n      code\n    }\n    envInfo\n    enableRunCode\n    hasFrontendPreview\n    frontendPreviews\n  }\n}"
    ) {
        put("titleSlug", titleSlug)
    } ?: return null
    return obj.getObj(json, "data", "question") {
        thisLogger().error(it)
    }
}