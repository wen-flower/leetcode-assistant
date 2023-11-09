package icu.twtool.leetcode.api.model

import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.api.LeetCodeApi
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.enumerate.QuestionStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@Serializable
data class ProblemsetQuestionList(
    val hasMore: Boolean = false,
    val questions: List<Question> = emptyList(),
    val total: Int = 0
) : java.io.Serializable{

    @Serializable
    data class Question(
        val acRate: Double,
        val difficulty: QuestionDifficulty,
        val freqBar: Double,
        val frontendQuestionId: String,
        val isFavor: Boolean,
        val paidOnly: Boolean,
        val solutionNum: Int,
        val status: QuestionStatus,
        val title: String,
        val titleCn: String,
        val titleSlug: String
    ): java.io.Serializable
}

suspend fun LeetCodeApi.getProblemsetQuestionList(
    skip: Int = 0,
    limit: Int = 50,
    categorySlug: String = "",
    filters: JsonObjectBuilder.() -> Unit = {}
): ProblemsetQuestionList? {
    var obj = graphql<JsonObject>(
        "problemsetQuestionList",
        "\n    query problemsetQuestionList(\$categorySlug: String, \$limit: Int, \$skip: Int, \$filters: QuestionListFilterInput) {\n  problemsetQuestionList(\n    categorySlug: \$categorySlug\n    limit: \$limit\n    skip: \$skip\n    filters: \$filters\n  ) {\n    hasMore\n    total\n    questions {\n      acRate\n      difficulty\n      freqBar\n      frontendQuestionId\n      isFavor\n      paidOnly\n      solutionNum\n      status\n      title\n      titleCn\n      titleSlug\n      topicTags {\n        name\n        nameTranslated\n        id\n        slug\n      }\n      extra {\n        hasVideoSolution\n        topCompanyTags {\n          imgUrl\n          slug\n          numSubscribed\n        }\n      }\n    }\n  }\n}\n    ",
    ) {
        put("categorySlug", categorySlug)
        put("limit", limit)
        put("skip", skip)
        putJsonObject("filters") {
            filters()
        }
    } ?: return null
    obj = (obj["data"] as? JsonObject) ?: return null
    obj = (obj["problemsetQuestionList"] as? JsonObject) ?: return null
    try {
        return json.decodeFromJsonElement(obj)
    } catch (e: Exception) {
        thisLogger().error(e)
        return null
    }
}