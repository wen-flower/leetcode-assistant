package icu.twtool.leetcode.util


fun Any.loadResourceText(name: String): String? = javaClass.getResource(name)?.readText()