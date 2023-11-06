package icu.twtool.leetcode.util

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import icons.LeetCodeIcons
import icu.twtool.leetcode.MyBundle

private const val NOTIFICATION_GROUP = "LEETCODE-ASSISTANT-NOTIFICATION"
private val notification: NotificationGroup
    get() = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP)

fun infoNotify(project: Project?, content: String) {
    notification.createNotification(content, NotificationType.INFORMATION)
        .setIcon(LeetCodeIcons.LeetCode)
        .notify(project)
}

fun errorNotify(project: Project?, content: String) {
    notification.createNotification(content, NotificationType.ERROR)
        .setIcon(LeetCodeIcons.LeetCode)
        .notify(project)
}