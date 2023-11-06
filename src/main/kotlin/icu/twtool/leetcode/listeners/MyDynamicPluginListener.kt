package icu.twtool.leetcode.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.diagnostic.thisLogger
import icu.twtool.leetcode.constants.PluginConstants

class MyDynamicPluginListener : DynamicPluginListener {

    private val log = thisLogger()

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString == PluginConstants.ID) {
            log.info("before unload...")
        }
    }
}