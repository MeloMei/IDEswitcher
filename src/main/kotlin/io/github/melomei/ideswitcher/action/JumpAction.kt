package io.github.melomei.ideswitcher.action

import io.github.melomei.ideswitcher.settings.IdeSwitcherSettings
import io.github.melomei.ideswitcher.target.CodeFuseJumper
import io.github.melomei.ideswitcher.target.CursorJumper
import io.github.melomei.ideswitcher.target.Jumper
import io.github.melomei.ideswitcher.target.QoderJumper
import io.github.melomei.ideswitcher.target.Target
import io.github.melomei.ideswitcher.target.TraeJumper
import io.github.melomei.ideswitcher.target.WindsurfJumper
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo

class JumpAction : AnAction() {

    private val logger = Logger.getInstance(JumpAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && SystemInfo.isMac
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if (!SystemInfo.isMac) {
            notifyError(project, "Unsupported Platform",
                "IDEswitcher only supports macOS.\nCurrent OS: ${SystemInfo.OS_NAME}")
            return
        }

        val target = IdeSwitcherSettings.getInstance().state.target
        val jumper: Jumper = when (target) {
            Target.QODER -> QoderJumper
            Target.CODEFUSE -> CodeFuseJumper
            Target.CURSOR -> CursorJumper
            Target.WINDSURF -> WindsurfJumper
            Target.TRAE -> TraeJumper
        }

        if (!jumper.isInstalled()) {
            notifyError(project,
                "${jumper.profile.displayName} Not Found",
                "Could not find ${jumper.profile.displayName}. " +
                    "Install it, or set a custom path in Settings → Tools → IDEswitcher.")
            return
        }

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        val line = editor?.caretModel?.currentCaret?.logicalPosition?.line?.plus(1)
        val column = editor?.caretModel?.currentCaret?.logicalPosition?.column?.plus(1)
        val projectPath = project.basePath ?: run {
            notifyError(project, "Jump Failed", "Cannot determine project path.")
            return
        }
        val filePath = if (file?.isDirectory == false) file.path else null

        try {
            jumper.jump(projectPath, filePath, line, column)
        } catch (ex: Exception) {
            logger.warn("Failed to jump to ${jumper.profile.displayName}", ex)
            notifyError(project,
                "Jump to ${jumper.profile.displayName} Failed",
                ex.message ?: "Unknown error")
        }
    }

    private fun notifyError(
        project: com.intellij.openapi.project.Project,
        title: String,
        content: String,
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("IDEswitcher")
            .createNotification(title, content, NotificationType.ERROR)
            .notify(project)
    }
}
