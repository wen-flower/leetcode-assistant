package icu.twtool.leetcode.ui.component

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.model.ProblemsetQuestionList.Question
import icu.twtool.leetcode.constants.MyColor
import java.awt.BorderLayout
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class QuestionListComponent(dataModel: QuestionListModel) : JBList<Question>(dataModel), Disposable {

    private val log = thisLogger()

    init {
        setCellRenderer { list, question, index, selected, hasFocus ->
            QuestionItemComponent(question, selected, hasFocus)
        }
//        setCellRenderer(object : ColoredListCellRenderer<Question>() {
//            override fun customizeCellRenderer(
//                list: JList<out Question>,
//                value: Question?,
//                index: Int,
//                selected: Boolean,
//                hasFocus: Boolean
//            ) {
//
//            }
//        })
    }

    override fun dispose() {

    }
}