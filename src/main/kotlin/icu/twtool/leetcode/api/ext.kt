package icu.twtool.leetcode.api

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

inline fun <reified T> JsonObject.getObj(json: Json, vararg fields: String, exceptionHandler: (Exception) -> Unit = {}): T? {
    var res: JsonObject? = this
    fields.forEach {
        res = res?.get(it) as JsonObject?
        if (res == null) {
            thisLogger().warn("res == null, it = $it")
            return null
        }
    }
    try {
        return json.decodeFromJsonElement(res ?: return null)
    } catch (e: Exception) {
        exceptionHandler(e)
        return null
    }
}