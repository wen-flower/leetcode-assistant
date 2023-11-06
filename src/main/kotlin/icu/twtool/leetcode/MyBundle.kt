package icu.twtool.leetcode

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.MyBundle"

object MyBundle: DynamicBundle(BUNDLE) {

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getMessage(key, *params)

    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)

    fun getDevToolButtonText(): String = message("button.dev.tools")
    fun getLoggedButtonText(): String = message("button.logged")
    fun getBackButtonText(): String = message("button.back")
}