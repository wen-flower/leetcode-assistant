package icu.twtool.leetcode.api.model

import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.api.LeetCodeApi
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.enumerate.QuestionStatus
import icu.twtool.leetcode.api.getObj
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@Serializable
data class ConsolePanelConfig(
    val questionId: String,
    val questionFrontendId: String,
    val questionTitle: String,
    val enableRunCode: Boolean,
    val enableSubmit: Boolean,
    val enableTestMode: Boolean,
    val jsonExampleTestcases: String,
    val exampleTestcases: String,
    val metaData: String,
    val sampleTestCase: String
) : java.io.Serializable {

    @Serializable
    data class MetaData(
        val name: String,
        val params: List<Param>,
        @SerialName("return")
        val returnInfo: ReturnInfo? = null,
        val manual: Boolean
    ) : java.io.Serializable

    @Serializable
    data class Param(
        val name: String,
        val type: String
    ) : java.io.Serializable

    @Serializable
    data class ReturnInfo(
        val type: String,
        val size: Int
    ) : java.io.Serializable
}

suspend fun LeetCodeApi.getConsolePanelConfig(titleSlug: String): ConsolePanelConfig? {
    val obj = graphql<JsonObject>(
        "consolePanelConfig",
        "query consolePanelConfig(\$titleSlug: String!) {\n  question(titleSlug: \$titleSlug) {\n    questionId\n    questionFrontendId\n    questionTitle\n    enableRunCode\n    enableSubmit\n    enableTestMode\n    jsonExampleTestcases\n    exampleTestcases\n    metaData\n    sampleTestCase\n  }\n}\n"
    ) {
        put("titleSlug", titleSlug)
    } ?: return null
    return obj.getObj(json, "data", "question") {
        thisLogger().error(it)
    }
}