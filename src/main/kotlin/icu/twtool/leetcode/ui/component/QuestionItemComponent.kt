package icu.twtool.leetcode.ui.component

import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import icons.LeetCodeIcons
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.enumerate.QuestionStatus
import icu.twtool.leetcode.api.model.ProblemsetQuestionList.Question
import icu.twtool.leetcode.constants.MyColor
import java.awt.BorderLayout

class QuestionItemComponent(question: Question, selected: Boolean, hasFocus: Boolean) : BorderLayoutPanel() {

    private val color = when (question.difficulty) {
        QuestionDifficulty.EASY -> MyColor.Easy
        QuestionDifficulty.MEDIUM -> MyColor.Medium
        QuestionDifficulty.HARD -> MyColor.Hard
        else -> null
    }

    init {
        val cellPadding = UIUtil.getListCellPadding()

        val icon = JBLabel()
        icon.icon = when (question.status) {
            QuestionStatus.AC -> LeetCodeIcons.AC
            QuestionStatus.TRIED -> LeetCodeIcons.Tried
            else -> LeetCodeIcons.NoStart
        }
        icon.border = JBUI.Borders.emptyRight(cellPadding.right)

        val title = JBLabel("${question.frontendQuestionId}. ${question.titleCn}")

        color?.let {
            title.foreground = it
        }

        if (selected) background = UIUtil.getListSelectionBackground(hasFocus)

        border = JBUI.Borders.empty(cellPadding)

        add(icon, BorderLayout.WEST)
        add(title, BorderLayout.CENTER)
    }
}