package icu.twtool.leetcode.api.model

import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.api.LeetCodeApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

@Serializable
data class QuestionTranslations(
    val translatedContent: String,
    val translatedTitle: String
) : java.io.Serializable

suspend fun LeetCodeApi.getQuestionTranslations(titleSlug: String): QuestionTranslations? {
    var obj = graphql<JsonObject>(
        "questionTranslations",
        "query questionTranslations(\$titleSlug: String!) {\n  question(titleSlug: \$titleSlug) {\n    translatedTitle\n    translatedContent\n  }\n}",
    ) {
        put("titleSlug", titleSlug)
    } ?: return null
    obj = (obj["data"] as? JsonObject) ?: return null
    obj = (obj["question"] as? JsonObject) ?: return null
    try {
        return json.decodeFromJsonElement(obj)
    } catch (e: Exception) {
        thisLogger().error(e)
        return null
    }
}