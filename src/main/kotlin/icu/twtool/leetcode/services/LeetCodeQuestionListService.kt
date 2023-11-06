package icu.twtool.leetcode.services

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import icu.twtool.leetcode.api.enumerate.QuestionDifficulty
import icu.twtool.leetcode.api.enumerate.QuestionStatus
import icu.twtool.leetcode.api.model.getProblemsetQuestionList
import icu.twtool.leetcode.listeners.notifier.QuestionListUpdateNotifier
import icu.twtool.leetcode.ui.component.QuestionListModel
import icu.twtool.leetcode.util.application
import icu.twtool.leetcode.util.createCoroutinesScope
import icu.twtool.leetcode.util.getService
import icu.twtool.leetcode.util.messageBusConnect
import icu.twtool.leetcode.util.syncPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlin.math.max
import kotlin.math.min

@Service(Service.Level.PROJECT)
class LeetCodeQuestionListService: Disposable {

    companion object {
        fun getInstance(): LeetCodeQuestionListService = application.getService()
    }

    private val scope = createCoroutinesScope(Dispatchers.IO)

    private val requestService by lazy { LeetCodeRequestService.getInstance() }

    private val mutex = Mutex()

    val loading: Boolean get() = mutex.isLocked

    var skip = 0
        private set
    var limit = 50
        private set
    var total = 0
        private set

    val dataModel = QuestionListModel()

    var filters = Filters()

    suspend fun fetchData(skip: Int = this.skip): Boolean {
        return fetchData(skip, this.limit)
    }

    fun asyncFetchData(skip: Int = this.skip) {
        scope.launch {
            fetchData(skip)
        }
    }

    private suspend fun fetchData(skip: Int = this.skip, limit: Int = this.limit): Boolean {
        mutex.lock()
        QuestionListUpdateNotifier.TOPIC.syncPublisher().updateQuestionListState()
        val result = try {
            val result = requestService.api.getProblemsetQuestionList(skip, limit) {
                filters.injectJsonObject(this)
            }
            if (result != null) {
                this.total = result.total
                this.skip = skip
                this.limit = limit
                application.invokeLater {
                    this.dataModel.update(result.questions)
                    ActivityTracker.getInstance().inc()
                }
                true
            } else false
        } catch (_: Exception) {
            false
        } finally {
            mutex.unlock()
        }
        QuestionListUpdateNotifier.TOPIC.syncPublisher().updateQuestionListState(result)
        return result
    }

    suspend fun previous(): Boolean {
        val skip = max(this.skip - limit, 0)
        if (this.skip == skip) return true

        return fetchData(skip)
    }

    suspend fun refresh(limit: Int = this.limit): Boolean {
        val skip =
            if (this.limit == limit) this.skip
            else min(this.skip / limit, total / limit) * limit

        return fetchData(skip, limit)
    }

    suspend fun next(): Boolean {
        val skip = max(this.skip + limit, 0)
        if (skip >= this.total) return true

        return fetchData(skip)
    }

    override fun dispose() {
        scope.cancel()
    }

    data class Filters(
        var difficultyEnabled: Boolean = false,
        var difficulty: QuestionDifficulty = QuestionDifficulty.EASY,

        var statusEnable: Boolean = false,
        var status: QuestionStatus = QuestionStatus.NOT_STARTED,

        var searchKeywords: String = "",
    ) {

        val enabled: Boolean get() = difficultyEnabled || statusEnable

        fun injectJsonObject(builder: JsonObjectBuilder) = builder.apply {
            if (difficultyEnabled) put("difficulty", difficulty.name)
            if (statusEnable) put("status", status.name)
            val searchKeywords = searchKeywords.trim()
            if (searchKeywords.isNotBlank()) put("searchKeywords", searchKeywords)
        }
    }
}