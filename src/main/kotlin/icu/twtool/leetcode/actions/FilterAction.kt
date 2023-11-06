package icu.twtool.leetcode.actions

import com.intellij.execution.runners.IndicatorIcon
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.LayeredIcon
import com.vladsch.flexmark.util.html.ui.Color
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.ui.component.QuestionListFilterComponent
import javax.swing.Icon

class FilterAction : AnAction(), Disposable {

    private val log = thisLogger()

    private val questionListService by lazy { LeetCodeQuestionListService.getInstance() }

    private val filterComponent by lazy { QuestionListFilterComponent() }

    private fun createPopup(): JBPopup {
        return JBPopupFactory.getInstance()
            .createComponentPopupBuilder(filterComponent, null)
            .createPopup()
    }

    private var icon: Icon = LayeredIcon.create(AllIcons.General.Filter, AllIcons.General.Dropdown)
    private var indicatorIcon = LayeredIcon.create(AllIcons.General.Filter, IndicatorIcon(null, icon.iconWidth, icon.iconHeight, Color.GREEN))

    override fun actionPerformed(e: AnActionEvent) {
        val component = e.inputEvent?.component
        if (component != null) createPopup().showUnderneathOf(component)
        else e.project?.let { createPopup().showCenteredInCurrentWindow(it) }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = !questionListService.loading
        e.presentation.icon = if (questionListService.filters.enabled) indicatorIcon else icon
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun dispose() {
        filterComponent.dispose()
    }
}