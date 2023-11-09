package icu.twtool.leetcode.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.twtool.leetcode.services.LeetCodeRequestService
import icu.twtool.leetcode.util.createCoroutinesScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LeetCodeEditorProvider : FileEditorProvider, DumbAware, Disposable {

    private val scope = createCoroutinesScope(Dispatchers.IO)
    private val requestService by lazy { LeetCodeRequestService.getInstance() }

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return FileTypeRegistry.getInstance().isFileOfType(file, LeetCodeFileType.INSTANCE)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val editor = LeetCodeFileEditor(project, file, scope)
        return editor
    }

    override fun getEditorTypeId(): String = "LeetCode.Editor.Provider"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    override fun dispose() {
        scope.cancel()
    }
}