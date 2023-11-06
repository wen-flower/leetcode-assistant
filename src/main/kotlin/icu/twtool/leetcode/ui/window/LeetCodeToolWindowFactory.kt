package icu.twtool.leetcode.ui.window

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.impl.ContentImpl
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.constants.PluginConstants

class LeetCodeToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val toolWindowImpl = LeetCodeToolWindowImpl(project, toolWindow)
        contentManager.addContent(ContentImpl(toolWindowImpl, "", true))

        val actionManager = ActionManager.getInstance()
        val actionGroup = actionManager.getAction(PluginConstants.TOOL_WINDOW_HEADER_GROUP_ACTION_ID) as ActionGroup

        toolWindow.setTitleActions(mutableListOf(actionGroup))
    }

    override fun shouldBeAvailable(project: Project): Boolean = true // 始终显示工具窗口

}