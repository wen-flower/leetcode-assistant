package icu.twtool.leetcode.ui.dialogs

import com.intellij.execution.runners.IndicatorIcon
import com.intellij.openapi.editor.colors.impl.AppEditorFontOptions
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.util.minimumHeight
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.api.model.Check
import icu.twtool.leetcode.api.model.ConsolePanelConfig
import icu.twtool.leetcode.constants.MyColor
import icu.twtool.leetcode.ui.component.RoundedBorderLayoutPanel
import java.awt.FlowLayout
import javax.swing.Action
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 测试结果弹窗
 */
class TestResultDialog(
    private val project: Project,
    private val result: Check,
    private val metaData: ConsolePanelConfig.MetaData,
    private val testCase: String
) : DialogWrapper(project, false) {

    companion object {

        private const val MINI_FONT = 12f
    }

    private val centerPanel = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        createTitle()
        createResult()
    }

    init {
        title = MyBundle.message("test.result")
        setOKButtonText(MyBundle.message("button.close"))
        setSize(JBUI.scale(600), JBUI.scale(500))
        isResizable = false
        init()
    }

    private fun JBPanel<*>.createTitle() {
        val spacing = JBUI.scale(16)
        val flowPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT))

        JBLabel().let {
            it.text = MyBundle.message(
                when (result.statusCode) {
                    Check.AC_CODE -> if (result.correctAnswer == true) "result.accepted" else "result.answer.error"
                    Check.COMPILER_ERROR_CODE -> "result.compiler.error"
                    Check.RUNTIME_ERROR_CODE -> "result.runtime.error"
                    else -> "run.failure"
                }
            )

            it.font = JBUI.Fonts.label(24f)
            it.foreground = if (result.correctAnswer == true) MyColor.Accepted
            else MyColor.NotAccepted

            flowPanel.add(it)
        }

        result.statusRuntime?.let { runtime ->
            JBLabel().let {
                it.border = JBUI.Borders.emptyLeft(spacing)
                it.text = MyBundle.message("label.runtime", runtime)
                it.foreground = JBColor.namedColor("Label.infoForeground")
                flowPanel.add(it)
            }
        }

        flowPanel.add(Box.createHorizontalGlue())
        add(flowPanel)
    }

    private fun JBPanel<*>.createResult() {
        val borderLayoutPanel = BorderLayoutPanel()

        val compilerOrRuntimeError =
            result.fullCompileError ?: result.compileError
            ?: result.fullRuntimeError ?: result.runtimeError

        val compareResult = result.compareResult?.reversed()?.toInt(2)
        val repeat = result.totalTestcases


        if (compilerOrRuntimeError != null) {
            JBTextArea(compilerOrRuntimeError).let {
                it.border = JBUI.Borders.empty(16)
                it.foreground = MyColor.ErrorForeground
                it.background = MyColor.ErrorBackground
                val fontPreferences = AppEditorFontOptions.getInstance().state
                it.font = JBUI.Fonts.create(fontPreferences.FONT_FAMILY, fontPreferences.FONT_SIZE)
                it.isEditable = false
                borderLayoutPanel.addToCenter(JBScrollPane(it).also { pane ->
                    pane.border = JBUI.Borders.empty()
                })
            }
        } else if (compareResult != null && repeat != null) {
            val size = JBUI.scale(4)
            val acceptedIcon = IndicatorIcon(null, size, size, MyColor.Accepted)
            val notAcceptedIcon = IndicatorIcon(null, size, size, MyColor.NotAccepted)

            val codeAnswer = result.codeAnswer
            val exceptedCodeAnswer = result.expectedCodeAnswer
            val inputLines = testCase.split("\n")
            val params = metaData.params.map {
                it.name
            }

            JBTabsFactory.createTabs(project).let {
                for (index in 0..<repeat) {
                    val input = List(params.size) { i: Int ->
                        inputLines.getOrNull(index * params.size + i) ?: ""
                    }
                    val answer = codeAnswer?.getOrNull(index) ?: ""
                    val expected = exceptedCodeAnswer?.getOrNull(index) ?: ""
                    val info = TabInfo(createDiffComponent(input, params, answer, expected))
                    info.text = "Case ${index + 1}"
                    info.icon = if (compareResult.and(1.shl(index)) != 0) acceptedIcon else notAcceptedIcon

                    it.addTab(info)
                }

                borderLayoutPanel.addToCenter(it.component)
            }
        }

        add(borderLayoutPanel)
    }

    private fun createDiffComponent(
        input: List<String>,
        params: List<String>,
        answer: String,
        expected: String
    ): JComponent {
        val diffComponent = Box.createVerticalBox().apply {

        }

        // 输入
        val border = JBUI.Borders.empty(16, 0, 8, 0)
        Box.createHorizontalBox().let {
            it.border = border
            it.add(createInfoLabel(MyBundle.message("label.input")))
            it.add(Box.createHorizontalGlue())
            diffComponent.add(it)
        }
        params.forEachIndexed { index, param ->
            if (index != 0) {
                diffComponent.add(JPanel().apply { minimumHeight = JBUI.scale(4) })
            }
            createCodePanel(input[index], param).let {
                diffComponent.add(it)
            }
        }

        // 输出
        Box.createHorizontalBox().let {
            it.border = border
            it.add(createInfoLabel(MyBundle.message("label.output")))
            it.add(Box.createHorizontalGlue())
            diffComponent.add(it)
        }
        createCodePanel(answer).let {
            diffComponent.add(it)
        }

        // 预期结果
        Box.createHorizontalBox().let {
            it.border = border
            it.add(createInfoLabel(MyBundle.message("label.expected.result")))
            it.add(Box.createHorizontalGlue())
            diffComponent.add(it)
        }
        createCodePanel(expected).let {
            diffComponent.add(it)
        }

        diffComponent.add(JPanel().apply { minimumHeight = JBUI.scale(16) })


        return JBScrollPane(diffComponent)
    }

    private fun createCodePanel(code: String, title: String? = null): BorderLayoutPanel {
        return RoundedBorderLayoutPanel(JBUI.scale(8)).also {
            it.border = JBUI.Borders.empty(8)
            it.background = JBColor.namedColor("DefaultTabs.hoverBackground")

            if (title != null) it.addToTop(createInfoLabel("$title ="))
            it.addToCenter(JBLabel().also { label ->
                label.text = code
            })
        }
    }

    private fun createInfoLabel(text: String): JBLabel {
        return JBLabel(text).let {
            it.foreground = JBColor.namedColor("Label.infoForeground")
            it.font = JBUI.Fonts.label(MINI_FONT)

            it
        }
    }

    override fun createCenterPanel(): JComponent = centerPanel

    override fun createActions(): Array<Action> = arrayOf(okAction)
}