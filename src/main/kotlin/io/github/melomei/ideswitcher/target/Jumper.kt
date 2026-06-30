package io.github.melomei.ideswitcher.target

import io.github.melomei.ideswitcher.platform.Platform
import java.io.IOException
import java.util.concurrent.TimeUnit

interface Jumper {
    val profile: EditorProfile

    fun isInstalled(): Boolean = resolve() != null

    fun resolve(): EditorProfile.ResolvedPaths? {
        val customPath = try {
            io.github.melomei.ideswitcher.settings.IdeSwitcherSettings
                .getInstance().state.customPaths[profile.target.name]
        } catch (_: Exception) {
            null
        }
        return profile.resolvePath(customPath)
    }

    @Throws(IOException::class)
    fun jump(projectPath: String, filePath: String?, line: Int?, column: Int?) {
        val paths = resolve()
            ?: throw IOException("${profile.displayName} not found. Check installation or set a custom path in Settings → Tools → IDEswitcher.")

        val cli = java.io.File(paths.cliPath)
        if (!cli.exists()) {
            throw IOException("${profile.displayName} CLI not found: ${paths.cliPath}")
        }

        val cmd = buildCommand(paths.cliPath, projectPath, filePath, line, column)

        // On Windows, .cmd/.bat files must be launched via cmd /c
        val isWindowsCmd = Platform.current == Platform.WINDOWS &&
            (paths.cliPath.endsWith(".cmd", ignoreCase = true) ||
             paths.cliPath.endsWith(".bat", ignoreCase = true))
        val processCmd = when {
            isWindowsCmd -> arrayOf("cmd", "/c") + cmd
            Platform.current == Platform.LINUX && paths.cliPath.endsWith(".sh") ->
                arrayOf("/bin/sh") + cmd
            else -> cmd
        }

        val process = ProcessBuilder(processCmd.toList())
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()

        val finished = process.waitFor(5, TimeUnit.SECONDS)
        if (finished && process.exitValue() != 0) {
            val stderr = process.errorStream.bufferedReader().readText().trim()
            throw IOException(
                "${profile.displayName} exited with code ${process.exitValue()}" +
                    if (stderr.isNotEmpty()) ": $stderr" else ""
            )
        }
        process.outputStream.close()
    }

    fun buildCommand(
        cliPath: String,
        projectPath: String,
        filePath: String?,
        line: Int?,
        column: Int?
    ): Array<String> = when {
        filePath != null && line != null ->
            arrayOf(cliPath, "--goto", "$filePath:$line:${column ?: 1}")
        filePath != null -> arrayOf(cliPath, filePath)
        else -> arrayOf(cliPath, projectPath)
    }
}
