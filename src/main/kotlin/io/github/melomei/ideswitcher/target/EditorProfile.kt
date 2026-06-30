package io.github.melomei.ideswitcher.target

import io.github.melomei.ideswitcher.platform.Platform
import java.io.File

/**
 * Platform-specific path configuration for an editor.
 */
data class PlatformPath(
    val appDir: String,
    /** CLI path template. Use {appDir} for app directory substitution. */
    val cliPath: String,
    /** CLI binary names to search on $PATH, in priority order. */
    val cliBinaryNames: List<String>,
)

/**
 * Central registry of supported editors with multi-strategy, cross-platform path resolution.
 *
 * Resolution order (per platform):
 * 1. User-configured custom path (from Settings)
 * 2. Standard installation path (e.g. /Applications/ on macOS, %LOCALAPPDATA%\Programs\ on Windows)
 * 3. JetBrains Toolbox managed installation
 * 4. CLI binary found on $PATH / %PATH%
 */
enum class EditorProfile(
    val target: Target,
    val displayName: String,
    val macPath: PlatformPath,
    val windowsPath: PlatformPath,
    val linuxPath: PlatformPath,
) {
    QODER(
        target = Target.QODER, displayName = "Qoder",
        macPath = PlatformPath("/Applications/Qoder.app", "{appDir}/Contents/Resources/app/bin/code", listOf("code")),
        windowsPath = PlatformPath("%LOCALAPPDATA%\\Programs\\Qoder", "{appDir}\\bin\\code.cmd", listOf("code.cmd")),
        linuxPath = PlatformPath("/opt/qoder", "{appDir}/bin/code", listOf("qoder", "code")),
    ),
    CODEFUSE(
        target = Target.CODEFUSE, displayName = "CodeFuse",
        macPath = PlatformPath("/Applications/CodeFuse.app", "{appDir}/Contents/Resources/app/bin/code", listOf("code")),
        windowsPath = PlatformPath("%LOCALAPPDATA%\\Programs\\CodeFuse", "{appDir}\\bin\\code.cmd", listOf("code.cmd")),
        linuxPath = PlatformPath("/opt/codefuse", "{appDir}/bin/code", listOf("codefuse", "code")),
    ),
    CURSOR(
        target = Target.CURSOR, displayName = "Cursor",
        macPath = PlatformPath("/Applications/Cursor.app", "{appDir}/Contents/Resources/app/bin/code", listOf("code")),
        windowsPath = PlatformPath("%LOCALAPPDATA%\\Programs\\Cursor", "{appDir}\\bin\\code.cmd", listOf("code.cmd")),
        linuxPath = PlatformPath("/opt/cursor", "{appDir}/bin/code", listOf("cursor", "code")),
    ),
    WINDSURF(
        target = Target.WINDSURF, displayName = "Windsurf",
        macPath = PlatformPath("/Applications/Windsurf.app", "{appDir}/Contents/Resources/app/bin/code", listOf("code")),
        windowsPath = PlatformPath("%LOCALAPPDATA%\\Programs\\Windsurf", "{appDir}\\bin\\code.cmd", listOf("code.cmd")),
        linuxPath = PlatformPath("/opt/windsurf", "{appDir}/bin/code", listOf("windsurf", "code")),
    ),
    TRAE(
        target = Target.TRAE, displayName = "Trae",
        macPath = PlatformPath("/Applications/Trae.app", "{appDir}/Contents/Resources/app/bin/code", listOf("code")),
        windowsPath = PlatformPath("%LOCALAPPDATA%\\Programs\\Trae", "{appDir}\\bin\\code.cmd", listOf("code.cmd")),
        linuxPath = PlatformPath("/opt/trae", "{appDir}/bin/code", listOf("trae", "code")),
    );

    data class ResolvedPaths(val appPath: String, val cliPath: String)

    private fun currentPlatformPath(): PlatformPath = when (Platform.current) {
        Platform.MACOS -> macPath
        Platform.WINDOWS -> windowsPath
        Platform.LINUX -> linuxPath
    }

    private fun expandEnv(path: String): String {
        var result = path
        val envPattern = Regex("%([^%]+)%")
        result = envPattern.replace(result) { match ->
            System.getenv(match.groupValues[1]) ?: match.value
        }
        if (result.startsWith("~")) {
            result = System.getProperty("user.home") + result.substring(1)
        }
        return result
    }

    private fun resolveCliPath(pp: PlatformPath, appDir: String): String =
        expandEnv(pp.cliPath.replace("{appDir}", appDir))

    fun resolvePath(customPath: String? = null): ResolvedPaths? {
        val pp = currentPlatformPath()

        // Strategy 1: user override
        if (!customPath.isNullOrBlank()) {
            val expanded = expandEnv(customPath)
            val cli = resolveCliPath(pp, expanded)
            if (File(cli).exists()) return ResolvedPaths(expanded, cli)
            if (File(expanded).exists()) return ResolvedPaths(expanded, expanded)
        }

        // Strategy 2: standard installation path
        val standardApp = expandEnv(pp.appDir)
        val standardCli = resolveCliPath(pp, standardApp)
        if (File(standardApp).isDirectory && File(standardCli).exists()) {
            return ResolvedPaths(standardApp, standardCli)
        }

        // Strategy 3: JetBrains Toolbox
        val toolboxApp = toolboxAppPath()
        if (toolboxApp != null) {
            val toolboxCli = resolveCliPath(pp, toolboxApp)
            if (File(toolboxApp).isDirectory && File(toolboxCli).exists()) {
                return ResolvedPaths(toolboxApp, toolboxCli)
            }
        }

        // Strategy 4: CLI binary on $PATH / %PATH% (try each binary name in order)
        val pathEnv = System.getenv("PATH") ?: return null
        val separator = if (Platform.current == Platform.WINDOWS) ";" else ":"
        for (binaryName in pp.cliBinaryNames) {
            for (dir in pathEnv.split(separator)) {
                val candidate = File(dir, binaryName)
                if (candidate.exists()) {
                    return ResolvedPaths(dir, candidate.absolutePath)
                }
            }
        }

        return null
    }

    private fun toolboxAppPath(): String? {
        val home = System.getProperty("user.home") ?: return null
        return when (Platform.current) {
            Platform.MACOS -> {
                val base = "$home/Library/Application Support/JetBrains/Toolbox/apps"
                val dir = File(base, "$displayName.app")
                if (dir.isDirectory) dir.absolutePath else null
            }
            Platform.WINDOWS -> {
                val localAppData = System.getenv("LOCALAPPDATA") ?: return null
                val base = "$localAppData\\JetBrains\\Toolbox\\apps"
                for (product in listOf("IDEA-U", "IDEA-C")) {
                    val productDir = File(base, product)
                    if (productDir.isDirectory) {
                        productDir.listFiles()
                            ?.filter { it.isDirectory }
                            ?.sortedByDescending { it.name }
                            ?.forEach { ver ->
                                val ideaDir = File(ver, "IntelliJ IDEA")
                                if (ideaDir.isDirectory) return ideaDir.absolutePath
                            }
                    }
                }
                null
            }
            Platform.LINUX -> {
                val base = "$home/.local/share/JetBrains/Toolbox/apps"
                val dir = File(base, displayName)
                if (dir.isDirectory) dir.absolutePath else null
            }
        }
    }

    fun isInstalled(): Boolean = resolvePath() != null

    companion object {
        fun forTarget(target: Target): EditorProfile =
            entries.first { it.target == target }
    }
}
