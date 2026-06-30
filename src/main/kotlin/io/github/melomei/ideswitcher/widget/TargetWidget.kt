package io.github.melomei.ideswitcher.widget

import io.github.melomei.ideswitcher.settings.IdeSwitcherSettings
import io.github.melomei.ideswitcher.target.EditorProfile
import io.github.melomei.ideswitcher.target.Target
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class TargetWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = ID

    override fun getDisplayName(): String = "IDEswitcher Target"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = TargetWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {}

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    companion object {
        const val ID = "IDEswitcherTargetWidget"
    }
}

class TargetWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    private var statusBar: StatusBar? = null

    override fun ID(): String = TargetWidgetFactory.ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        statusBar = null
    }

    override fun getText(): String {
        val target = IdeSwitcherSettings.getInstance().state.target
        val profile = EditorProfile.forTarget(target)
        val installed = if (profile.isInstalled()) "" else " (!)"
        return "IDE: ${profile.displayName}$installed"
    }

    override fun getAlignment(): Float = 0f

    override fun getTooltipText(): String {
        val target = IdeSwitcherSettings.getInstance().state.target
        val installed = Target.entries.filter { EditorProfile.forTarget(it).isInstalled() }
        return "IDEswitcher: ${target.displayName} (click to cycle). Installed: ${installed.joinToString { it.displayName }}"
    }

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        cycleTarget()
        statusBar?.updateWidget(ID())
    }

    private fun cycleTarget() {
        val settings = IdeSwitcherSettings.getInstance()
        val currentTarget = settings.state.target
        val installedTargets = Target.entries.filter { EditorProfile.forTarget(it).isInstalled() }
        if (installedTargets.isEmpty()) return

        val currentIndex = installedTargets.indexOf(currentTarget)
        val nextIndex = if (currentIndex < 0 || currentIndex >= installedTargets.lastIndex) 0
                        else currentIndex + 1
        settings.state.target = installedTargets[nextIndex]
    }
}
