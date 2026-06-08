package io.github.meloluvmei.ideswitcher.settings

import io.github.meloluvmei.ideswitcher.target.Target
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
            val qoder = File("/Applications/Qoder.app").exists()
            val cf = File("/Applications/CodeFuse.app").exists()
            return when {
                qoder && !cf -> Target.QODER
                cf && !qoder -> Target.CODEFUSE
                else -> Target.QODER
            }
        }
    }
}
