package io.github.melomei.ideswitcher.action

import io.github.melomei.ideswitcher.settings.IdeSwitcherSettings
import io.github.melomei.ideswitcher.target.CodeFuseJumper
import io.github.melomei.ideswitcher.target.CursorJumper
import io.github.melomei.ideswitcher.target.Jumper
import io.github.melomei.ideswitcher.target.QoderJumper
import io.github.melomei.ideswitcher.target.Target
import io.github.melomei.ideswitcher.target.TraeJumper
import io.github.melomei.ideswitcher.target.WindsurfJumper
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
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
            Messages.showErrorDialog(
                project,
                "IDEswitcher only supports macOS.\nCurrent OS: ${SystemInfo.OS_NAME}",
                "Unsupported Platform"
            )
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
            Messages.showErrorDialog(
                project,
                "${jumper.displayName} not found at ${jumper.appPath}.\n" +
                    "Install it, or change the target in Settings → Tools → IDEswitcher.",
                "${jumper.displayName} Not Found"
            )
            return
        }

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        val line = editor?.caretModel?.currentCaret?.logicalPosition?.line?.plus(1)
        val column = editor?.caretModel?.currentCaret?.logicalPosition?.column?.plus(1)
        val projectPath = project.basePath ?: run {
            Messages.showErrorDialog(project, "Cannot determine project path.", "Jump Failed")
            return
        }
        val filePath = if (file?.isDirectory == false) file.path else null

        try {
            jumper.jump(projectPath, filePath, line, column)
        } catch (ex: Exception) {
            logger.error("Failed to jump to ${jumper.displayName}", ex)
            Messages.showErrorDialog(
                project,
                "Failed to open ${jumper.displayName}: ${ex.message}",
                "Jump Failed"
            )
        }
    }
}
