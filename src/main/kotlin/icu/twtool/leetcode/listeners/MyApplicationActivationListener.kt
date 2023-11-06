package icu.twtool.leetcode.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class MyApplicationActivationListener : ApplicationActivationListener {

    private val log = thisLogger()

    override fun applicationActivated(ideFrame: IdeFrame) {
        log.warn("application activated 131...")
    }
}