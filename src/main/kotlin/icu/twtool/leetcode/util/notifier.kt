package icu.twtool.leetcode.util

import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

fun messageBusConnect(): MessageBusConnection = application.messageBus.connect()

fun <T : Any> Topic<T>.syncPublisher() = application.messageBus.syncPublisher(this)