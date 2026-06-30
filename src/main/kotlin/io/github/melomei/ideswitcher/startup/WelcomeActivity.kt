package io.github.melomei.ideswitcher.startup

import io.github.melomei.ideswitcher.settings.IdeSwitcherSettings
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.SystemInfo

class WelcomeActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val settings = IdeSwitcherSettings.getInstance()
        if (settings.state.firstRunDone) return

        settings.state.firstRunDone = true

        val target = settings.state.target
        val shortcut = if (SystemInfo.isMac) "Option+Shift+O" else "Ctrl+Alt+O"
        val content = "Press $shortcut to jump between IntelliJ IDEA and ${target.displayName}.\n" +
            "Configure your target in Settings -> Tools -> IDEswitcher."

        NotificationGroupManager.getInstance()
            .getNotificationGroup("IDEswitcher")
            .createNotification("Welcome to IDEswitcher!", content, NotificationType.INFORMATION)
            .addAction(object : NotificationAction("Configure Target") {
                override fun actionPerformed(e: AnActionEvent, notification: com.intellij.notification.Notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "IDEswitcher")
                    notification.expire()
                }
            })
            .addAction(object : NotificationAction("Dismiss") {
                override fun actionPerformed(e: AnActionEvent, notification: com.intellij.notification.Notification) {
                    notification.expire()
                }
            })
            .notify(project)
    }
}
