package icu.twtool.leetcode.constants

import com.intellij.openapi.util.registry.RegistryManager

object MyRegistry {

    val debug: Boolean get() = RegistryManager.getInstance().`is`("leetcode.debug")
}