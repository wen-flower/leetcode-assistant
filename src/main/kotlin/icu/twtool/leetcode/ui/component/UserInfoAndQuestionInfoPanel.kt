package icu.twtool.leetcode.ui.component

import com.intellij.ui.components.JBLabel
import com.intellij.ui.util.minimumWidth
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.MyBundle
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Insets
import javax.swing.Box
import javax.swing.border.EmptyBorder
import kotlin.math.min

class UserInfoAndQuestionInfoPanel(
    private var username: String?,
    private var skip: Int,
    private var limit: Int,
    private var total: Int
) : BorderLayoutPanel() {

    private val userInfoLabel = JBLabel(parseUserInfo())
    private val questionInfoLabel = JBLabel(parseQuestionInfo())

    init {
//        border = JBUI.Borders.empty(JBUI.insets(0, 8))
//        border =  JBUI.Borders.customLineTop(UIUtil.getBoundsColor())
        border = JBUI.Borders.compound(
            JBUI.Borders.customLineTop(UIUtil.getBoundsColor()),
            JBUI.Borders.empty(4, 8),
        )
        add(userInfoLabel, BorderLayout.WEST)
        add(Box.createHorizontalGlue(), BorderLayout.CENTER)
        add(questionInfoLabel, BorderLayout.EAST)
        setBounds(0, 0, width, height)
    }

    private fun parseUserInfo(): String = username ?: MyBundle.message("please.login")
    private fun parseQuestionInfo(): String =
        MyBundle.message("label.question.info", skip + 1, min(skip + limit, total), total)

    fun update(username: String?, skip: Int, limit: Int, total: Int) {
        this.username = username
        this.skip = skip
        this.limit = limit
        this.total = total
        userInfoLabel.text = parseUserInfo()
        questionInfoLabel.text = parseQuestionInfo()
        validate()
        repaint()
    }
}