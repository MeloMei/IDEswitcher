package io.github.melomei.ideswitcher.extension

import com.intellij.openapi.extensions.ExtensionPointName
import io.github.melomei.ideswitcher.target.Jumper

/**
 * Extension point for third-party editor jumpers.
 *
 * External plugins can register additional jumpers by implementing this interface
 * and declaring it in their plugin.xml:
 *
 * ```
 * <extensions defaultExtensionNs="io.github.melomei.ideswitcher">
 *     <jumper implementation="com.example.MyEditorJumper"/>
 * </extensions>
 * ```
 *
 * See CONTRIBUTING.md for full instructions.
 */
interface JumperProvider {
    /** Unique identifier for this editor (e.g., "my-editor"). */
    val id: String

    /** Human-readable display name (e.g., "My Editor"). */
    val displayName: String

    /** The Jumper implementation that handles jumping to this editor. */
    fun createJumper(): Jumper
}

object JumperProviderRegistry {
    private val EP_NAME = ExtensionPointName.create<JumperProvider>("io.github.melomei.ideswitcher.jumper")

    /**
     * Get all registered jumper providers (built-in + third-party).
     */
    fun getAllProviders(): List<JumperProvider> = EP_NAME.extensionList

    /**
     * Find a provider by its ID.
     */
    fun findById(id: String): JumperProvider? = EP_NAME.extensionList.find { it.id == id }
}
