package icu.twtool.leetcode.api.model

import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.api.LeetCodeApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class UserStatus(
    val isSignedIn: Boolean,
    val isPremium: Boolean,
    val username: String?,
    val realName: String?,
    val avatar: String?,
    val userSlug: String?,
    val isAdmin: Boolean,
    val checkedInToday: Boolean,
    val useTranslation: Boolean,
    val premiumExpiredAt: Double?,
    val isTranslator: Boolean,
    val isSuperuser: Boolean,
    val isPhoneVerified: Boolean,
    val isVerified: Boolean,
)

suspend fun LeetCodeApi.getUserStatus(): UserStatus? {
    var obj = graphql<JsonObject>(
        "globalData",
        "query globalData {\n  userStatus {\n    isSignedIn\n    isPremium\n    username\n    realName\n    avatar\n    userSlug\n    isAdmin\n    checkedInToday\n    useTranslation\n    premiumExpiredAt\n    isTranslator\n    isSuperuser\n    isPhoneVerified\n    isVerified\n  }\n  jobsMyCompany {\n    nameSlug\n  }\n}",
        "graphql/noj-go"
    ) ?: return null
    obj = (obj["data"] as? JsonObject) ?: return null
    obj = (obj["userStatus"] as? JsonObject) ?: return null
    try {
        return json.decodeFromJsonElement(obj)
    } catch (e: Exception) {
        thisLogger().error(e)
        return null
    }
}