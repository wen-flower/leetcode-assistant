package icu.twtool.leetcode.util

import com.intellij.ide.extensionResources.ExtensionsRootType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import icu.twtool.leetcode.constants.PluginConstants
import java.nio.file.Path

val pluginRootPath: Path by lazy { ExtensionsRootType.getInstance().findResourceDirectory(PluginId.getId(PluginConstants.ID), "", true) }

val pluginRootVirtualFile: VirtualFile by lazy { LocalFileSystem.getInstance().findFileByNioFile(pluginRootPath)!! }