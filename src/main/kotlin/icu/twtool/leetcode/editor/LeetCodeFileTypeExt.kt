package icu.twtool.leetcode.editor

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import icu.twtool.leetcode.api.model.ConsolePanelConfig
import icu.twtool.leetcode.api.model.ProblemsetQuestionList
import icu.twtool.leetcode.api.model.QuestionEditorData
import icu.twtool.leetcode.api.model.QuestionTranslations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeBytes

data class LeetCodeFileBinaryData(
    var initialized: Boolean,
    var question: ProblemsetQuestionList.Question,
    var editorData: QuestionEditorData? = null,
    var translations: QuestionTranslations? = null,
    var consolePanelConfig: ConsolePanelConfig? = null
) : Serializable

private val CACHE_DATA_KEY = Key.create<LeetCodeFileBinaryData>("LeetCode.File.Binary.Data.Key")

suspend fun VirtualFile.readData(): LeetCodeFileBinaryData? {
    val data = getUserData(CACHE_DATA_KEY)
    if (data != null) return data

    return withContext(Dispatchers.IO) {

        var objectInputStream: ObjectInputStream? = null
        try {
            objectInputStream = ObjectInputStream(inputStream)
            (objectInputStream.readObject() as? LeetCodeFileBinaryData).apply {
                putUserData(CACHE_DATA_KEY, this)
            }
        } catch (e: Exception) {
            null
        } finally {
            objectInputStream?.close()
        }
    }
}

/**
 * 需要运行的 EDT
 */
suspend fun VirtualFile.writeData(data: LeetCodeFileBinaryData) {
    withContext(Dispatchers.IO) {
        val byteOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteOutputStream)
        objectOutputStream.writeObject(data)

        putUserData(CACHE_DATA_KEY, data)

        writeAction {
            setBinaryContent(byteOutputStream.toByteArray())
        }

        objectOutputStream.close()
        byteOutputStream.close()
    }
}

fun Path.initData(question: ProblemsetQuestionList.Question) {
    val byteOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteOutputStream)

    objectOutputStream.writeObject(
        LeetCodeFileBinaryData(
            initialized = false,
            question = question
        )
    )

    writeBytes(byteOutputStream.toByteArray(), StandardOpenOption.WRITE)
    objectOutputStream.close()
    byteOutputStream.close()
}