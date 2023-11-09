package icu.twtool.leetcode.editor

import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class LeetCodeEditorTabTitleProvider : EditorTabTitleProvider {

    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? =
        if (file.fileType == LeetCodeFileType.INSTANCE) file.nameWithoutExtension
        else null

    override fun getEditorTabTooltipText(project: Project, file: VirtualFile): String? =
        if (file.fileType == LeetCodeFileType.INSTANCE) file.nameWithoutExtension
        else null
}