package icu.twtool.leetcode.ui.component

import icu.twtool.leetcode.api.model.ProblemsetQuestionList.Question
import javax.swing.AbstractListModel
import kotlin.math.max

class QuestionListModel(private var items: List<Question> = emptyList()) : AbstractListModel<Question>() {

    fun update(items: List<Question>) {
        val oldSize = size
        this.items = items
        if (oldSize > 0) fireIntervalRemoved(this, 0, oldSize - 1)
        if (size > 0) fireIntervalAdded(this, 0, size - 1)
    }

    override fun getSize(): Int = items.size

    override fun getElementAt(index: Int): Question? = items.getOrNull(index)
}