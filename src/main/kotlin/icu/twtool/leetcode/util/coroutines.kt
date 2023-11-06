package icu.twtool.leetcode.util

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

fun Any.createCoroutinesScope(context: CoroutineContext) = CoroutineScope(context + CoroutineExceptionHandler { _, throwable ->
    thisLogger().error(throwable)
})