package io.github.melomei.ideswitcher.target

import java.io.File

/**
 * Central registry of supported editors with multi-strategy path resolution.
 *
 * Resolution order:
 * 1. User-configured custom path (from Settings)
 * 2. Standard /Applications/ installation
 * 3. JetBrains Toolbox managed installation
 * 4. CLI binary found on $PATH
 */
enum class EditorProfile(
    val target: Target,
    val displayName: String,
    val appBundleName: String,
    val cliRelativePath: String,
    val cliBinaryName: String,
) {
    QODER(
        target = Target.QODER,
        displayName = "Qoder",
        appBundleName = "Qoder.app",
        cliRelativePath = "Contents/Resources/app/bin/code",
        cliBinaryName = "code",
    ),
    CODEFUSE(
        target = Target.CODEFUSE,
        displayName = "CodeFuse",
        appBundleName = "CodeFuse.app",
        cliRelativePath = "Contents/Resources/app/bin/code",
        cliBinaryName = "code",
    ),
    CURSOR(
        target = Target.CURSOR,
        displayName = "Cursor",
        appBundleName = "Cursor.app",
        cliRelativePath = "Contents/Resources/app/bin/code",
        cliBinaryName = "code",
    ),
    WINDSURF(
        target = Target.WINDSURF,
        displayName = "Windsurf",
        appBundleName = "Windsurf.app",
        cliRelativePath = "Contents/Resources/app/bin/code",
        cliBinaryName = "code",
    ),
    TRAE(
        target = Target.TRAE,
        displayName = "Trae",
        appBundleName = "Trae.app",
        cliRelativePath = "Contents/Resources/app/bin/code",
        cliBinaryName = "code",
    );

    data class ResolvedPaths(val appPath: String, val cliPath: String)

    /**
     * Resolve editor paths using a multi-strategy chain.
     *
     * @param customPath user-configured override (may be null or blank)
     * @return resolved app + CLI paths, or null if not found anywhere
     */
    fun resolvePath(customPath: String? = null): ResolvedPaths? {
        // Strategy 1: user override
        if (!customPath.isNullOrBlank()) {
            val cli = "$customPath/$cliRelativePath"
            if (File(cli).canExecute()) {
                return ResolvedPaths(customPath, cli)
            }
        }

        // Strategy 2: standard /Applications/ installation
        val standardApp = "/Applications/$appBundleName"
        if (File(standardApp).isDirectory) {
            return ResolvedPaths(standardApp, "$standardApp/$cliRelativePath")
        }

        // Strategy 3: JetBrains Toolbox managed installation
        val toolboxApp = "${System.getProperty("user.home")}/Library/Application Support/JetBrains/Toolbox/apps/$appBundleName"
        if (File(toolboxApp).isDirectory) {
            return ResolvedPaths(toolboxApp, "$toolboxApp/$cliRelativePath")
        }

        // Strategy 4: CLI binary on $PATH
        val pathEnv = System.getenv("PATH") ?: return null
        for (dir in pathEnv.split(File.pathSeparator)) {
            val candidate = File(dir, cliBinaryName)
            if (candidate.canExecute()) {
                return ResolvedPaths(dir, candidate.absolutePath)
            }
        }

        return null
    }

    /**
     * Quick check whether the editor appears to be installed.
     * Used for the default-target picker on first launch.
     */
    fun isInstalled(): Boolean = resolvePath() != null

    companion object {
        fun forTarget(target: Target): EditorProfile =
            entries.first { it.target == target }
    }
}
