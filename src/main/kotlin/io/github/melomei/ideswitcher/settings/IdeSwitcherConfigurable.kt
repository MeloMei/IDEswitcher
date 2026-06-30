package io.github.melomei.ideswitcher.settings

import io.github.melomei.ideswitcher.target.EditorProfile
import io.github.melomei.ideswitcher.target.Target
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.table.DefaultTableModel

class IdeSwitcherConfigurable : Configurable {

    private val radioButtons = Target.entries.associateWith { JBRadioButton(it.displayName) }
    private val buttonGroup = ButtonGroup().apply {
        radioButtons.values.forEach { add(it) }
    }

    private val customPathField = TextFieldWithBrowseButton().apply {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("Select Editor Application Path")
            .withDescription("Choose the .app bundle or installation directory")
        addBrowseFolderListener(TextBrowseFolderListener(descriptor))
    }
    private val customPathLabel = JLabel("Custom path for selected editor (optional):")

    // Editor status table
    private val tableModel = object : DefaultTableModel(
        arrayOf("Editor", "Status", "Resolved Path"),
        0
    ) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    private val statusTable = JBTable(tableModel).apply {
        setShowGrid(false)
        tableHeader.reorderingAllowed = false
    }

    private var rootPanel: JPanel? = null
    private var selectedTargetForPath: Target = Target.QODER

    override fun getDisplayName(): String = "IDEswitcher"

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout(0, 8))

        // Top: target selection + custom path
        val topPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            gridx = 0
            anchor = GridBagConstraints.LINE_START
            insets = Insets(4, 4, 4, 4)
        }

        gbc.gridy = 0
        topPanel.add(JLabel("Default jump target:"), gbc)

        Target.entries.forEachIndexed { index, target ->
            gbc.gridy = index + 1
            val radio = radioButtons[target]!!
            topPanel.add(radio, gbc)
            // When radio selection changes, update the custom path field
            radio.addActionListener {
                selectedTargetForPath = target
                loadCustomPathForTarget(target)
            }
        }

        gbc.gridy = Target.entries.size + 1
        gbc.insets = Insets(12, 4, 4, 4)
        topPanel.add(customPathLabel, gbc)
        gbc.gridy = Target.entries.size + 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        topPanel.add(customPathField, gbc)

        panel.add(topPanel, BorderLayout.NORTH)

        // Bottom: editor status table
        refreshStatusTable()
        val scrollPane = JScrollPane(statusTable)
        scrollPane.preferredSize = java.awt.Dimension(0, 120)
        panel.add(scrollPane, BorderLayout.CENTER)

        // Spacer
        panel.add(JPanel(), BorderLayout.SOUTH)

        rootPanel = panel
        reset()
        return panel
    }

    private fun loadCustomPathForTarget(target: Target) {
        val settings = IdeSwitcherSettings.getInstance()
        customPathField.text = settings.state.customPaths[target.name].orEmpty()
    }

    private fun refreshStatusTable() {
        tableModel.rowCount = 0
        for (profile in EditorProfile.entries) {
            val resolved = profile.resolvePath()
            val status = if (resolved != null) "Installed" else "Not Found"
            val path = resolved?.appPath ?: "-"
            tableModel.addRow(arrayOf(profile.displayName, status, path))
        }
    }

    override fun isModified(): Boolean {
        val settings = IdeSwitcherSettings.getInstance()
        val currentTarget = settings.state.target
        val currentCustomPath = settings.state.customPaths[currentTarget.name].orEmpty()
        return selectedTarget() != currentTarget || customPathField.text != currentCustomPath
    }

    override fun apply() {
        val settings = IdeSwitcherSettings.getInstance()
        settings.state.target = selectedTarget()
        val customPath = customPathField.text.trim()
        val targetName = selectedTargetForPath.name
        if (customPath.isNotEmpty()) {
            settings.state.customPaths[targetName] = customPath
        } else {
            settings.state.customPaths.remove(targetName)
        }
        refreshStatusTable()
    }

    override fun reset() {
        val settings = IdeSwitcherSettings.getInstance()
        radioButtons[settings.state.target]?.isSelected = true
        selectedTargetForPath = settings.state.target
        loadCustomPathForTarget(settings.state.target)
        refreshStatusTable()
    }

    override fun disposeUIResources() {
        rootPanel = null
    }

    private fun selectedTarget(): Target =
        radioButtons.entries.firstOrNull { it.value.isSelected }?.key ?: Target.QODER
}
