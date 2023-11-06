package icu.twtool.leetcode.ui.component

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import icu.twtool.leetcode.MyBundle
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.enumerate.QuestionStatus
import icu.twtool.leetcode.listeners.notifier.QuestionListUpdateNotifier
import icu.twtool.leetcode.services.LeetCodeQuestionListService
import icu.twtool.leetcode.util.messageBusConnect
import javax.swing.JButton

class QuestionListFilterComponent : BorderLayoutPanel(), Disposable {

    private val messageBusConnection = messageBusConnect()
    private val questionListService = LeetCodeQuestionListService.getInstance()
    private var filters: LeetCodeQuestionListService.Filters
        get() = questionListService.filters
        set(value) {
            questionListService.filters = value
        }

    private val notLoading = object : ComponentPredicate() {
        override fun addListener(listener: (Boolean) -> Unit) {
            messageBusConnection.subscribe(QuestionListUpdateNotifier.TOPIC, object : QuestionListUpdateNotifier {
                override fun updateQuestionListState(result: Boolean?) {
                    listener(questionListService.loading.not())
                }
            })
        }

        override fun invoke(): Boolean = questionListService.loading.not()
    }

    private val applyButton: JButton = JButton(MyBundle.message("button.confirm")).apply {
        addActionListener {
            val flag = dialogPanel.isModified()
            dialogPanel.apply()
            if (flag) questionListService.asyncFetchData(0)
        }
    }

    private val clearButton: JButton = JButton(MyBundle.message("button.clear")).apply {
        addActionListener {
            val emptyFilter = LeetCodeQuestionListService.Filters()
            val flag = filters == emptyFilter
            if (!flag) filters = emptyFilter
            dialogPanel.reset()
            if (!flag) questionListService.asyncFetchData(0)
        }
    }

    private val dialogPanel: DialogPanel = panel {
        lateinit var difficultyCheckBox: Cell<JBCheckBox>
        lateinit var statusCheckBox: Cell<JBCheckBox>
        row {
            difficultyCheckBox = checkBox(MyBundle.message("filter.question.difficulty")).bindSelected(
                { filters.difficultyEnabled }, { filters.difficultyEnabled = it }
            )
        }
        indent {
            buttonsGroup {
                row {
                    radioButton(MyBundle.message("difficulty.easy"), QuestionDifficulty.EASY)
                    radioButton(MyBundle.message("difficulty.medium"), QuestionDifficulty.MEDIUM)
                    radioButton(MyBundle.message("difficulty.hard"), QuestionDifficulty.HARD)
                }
            }.enabledIf(difficultyCheckBox.selected).bind(
                { filters.difficulty }, { filters.difficulty = it }
            )
        }
        row {
            statusCheckBox = checkBox(MyBundle.message("filter.question.status")).bindSelected(
                { filters.statusEnable }, { filters.statusEnable = it }
            )
        }
        indent {
            buttonsGroup {
                row {
                    radioButton(MyBundle.message("status.not.start"), QuestionStatus.NOT_STARTED)
                    radioButton(MyBundle.message("status.ac"), QuestionStatus.AC)
                    radioButton(MyBundle.message("status.tried"), QuestionStatus.TRIED)
                }
            }.enabledIf(statusCheckBox.selected)
                .bind({ filters.status }, { filters.status = it })
        }
        row {
            cell(clearButton).align(AlignX.RIGHT).enabledIf(notLoading)
            cell(applyButton).align(AlignX.RIGHT).enabledIf(notLoading)
        }
    }

    init {
        border = JBUI.Borders.empty(16)

        add(dialogPanel)
    }

    override fun dispose() {
        messageBusConnection.dispose()
    }
}