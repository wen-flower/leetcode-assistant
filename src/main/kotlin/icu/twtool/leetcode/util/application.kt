package icu.twtool.leetcode.util

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager

val application: Application
    get() = ApplicationManager.getApplication()

inline fun <reified T> Application.getService(): T = getService(T::class.java)