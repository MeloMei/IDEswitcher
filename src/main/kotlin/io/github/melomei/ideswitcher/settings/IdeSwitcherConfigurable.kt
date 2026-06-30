package io.github.melomei.ideswitcher.settings

import io.github.melomei.ideswitcher.target.Target
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBRadioButton
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class IdeSwitcherConfigurable : Configurable {

    private val qoderRadio = JBRadioButton("Qoder")
    private val codeFuseRadio = JBRadioButton("CodeFuse")
    private val cursorRadio = JBRadioButton("Cursor")
    private val windsurfRadio = JBRadioButton("Windsurf")
    private val traeRadio = JBRadioButton("Trae")
    private val buttonGroup = ButtonGroup().apply {
        add(qoderRadio)
        add(codeFuseRadio)
        add(cursorRadio)
        add(windsurfRadio)
        add(traeRadio)
    }
    private var rootPanel: JPanel? = null

    override fun getDisplayName(): String = "IDEswitcher"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            gridx = 0
            anchor = GridBagConstraints.LINE_START
            insets = Insets(4, 4, 4, 4)
        }
        gbc.gridy = 0
        panel.add(JLabel("Default jump target:"), gbc)
        gbc.gridy = 1
        panel.add(qoderRadio, gbc)
        gbc.gridy = 2
        panel.add(codeFuseRadio, gbc)
        gbc.gridy = 3
        panel.add(cursorRadio, gbc)
        gbc.gridy = 4
        panel.add(windsurfRadio, gbc)
        gbc.gridy = 5
        panel.add(traeRadio, gbc)
        gbc.gridy = 6
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)
        rootPanel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val current = IdeSwitcherSettings.getInstance().state.target
        return selectedTarget() != current
    }

    override fun apply() {
        IdeSwitcherSettings.getInstance().state.target = selectedTarget()
    }

    override fun reset() {
        when (IdeSwitcherSettings.getInstance().state.target) {
            Target.QODER -> qoderRadio.isSelected = true
            Target.CODEFUSE -> codeFuseRadio.isSelected = true
            Target.CURSOR -> cursorRadio.isSelected = true
            Target.WINDSURF -> windsurfRadio.isSelected = true
            Target.TRAE -> traeRadio.isSelected = true
        }
    }

    override fun disposeUIResources() {
        rootPanel = null
    }

    private fun selectedTarget(): Target = when {
        codeFuseRadio.isSelected -> Target.CODEFUSE
        cursorRadio.isSelected -> Target.CURSOR
        windsurfRadio.isSelected -> Target.WINDSURF
        traeRadio.isSelected -> Target.TRAE
        else -> Target.QODER
    }
}
