package io.github.melomei.ideswitcher.settings

import io.github.melomei.ideswitcher.target.EditorProfile
import io.github.melomei.ideswitcher.target.Target
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "IdeSwitcherSettings", storages = [Storage("ide-switcher.xml")])
class IdeSwitcherSettings : PersistentStateComponent<IdeSwitcherSettings.State> {

    data class State(
        var target: Target = pickDefault(),
        /** Per-target custom app path override. Key is Target.name, value is path string. */
        var customPaths: MutableMap<String, String> = mutableMapOf(),
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(s: State) {
        state = s
    }

    companion object {
        fun getInstance(): IdeSwitcherSettings = service()

        private fun pickDefault(): Target {
            val found = EditorProfile.entries.filter { it.isInstalled() }
            return found.firstOrNull()?.target ?: Target.QODER
        }
    }
}
