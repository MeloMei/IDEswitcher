package io.github.melomei.ideswitcher.settings

import io.github.melomei.ideswitcher.target.Target
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.io.File

@Service(Service.Level.APP)
@State(name = "IdeSwitcherSettings", storages = [Storage("ide-switcher.xml")])
class IdeSwitcherSettings : PersistentStateComponent<IdeSwitcherSettings.State> {

    data class State(var target: Target = pickDefault())

    private var state = State()

    override fun getState(): State = state

    override fun loadState(s: State) {
        state = s
    }

    companion object {
        fun getInstance(): IdeSwitcherSettings = service()

        private fun pickDefault(): Target {
            val installed = mapOf(
                Target.QODER to File("/Applications/Qoder.app").exists(),
                Target.CODEFUSE to File("/Applications/CodeFuse.app").exists(),
                Target.CURSOR to File("/Applications/Cursor.app").exists(),
                Target.WINDSURF to File("/Applications/Windsurf.app").exists(),
                Target.TRAE to File("/Applications/Trae.app").exists(),
            )
            val found = installed.filterValues { it }.keys
            return when {
                found.size == 1 -> found.first()
                found.size > 1 -> found.first()
                else -> Target.QODER
            }
        }
    }
}
