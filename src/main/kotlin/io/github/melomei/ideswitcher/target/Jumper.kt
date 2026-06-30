package io.github.melomei.ideswitcher.target

import java.io.IOException
import java.util.concurrent.TimeUnit

interface Jumper {
    val profile: EditorProfile

    fun isInstalled(): Boolean {
        val resolved = resolve()
        return resolved != null
    }

    /** Current resolved paths (cached). Returns null if editor not found. */
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
        if (!cli.canExecute()) {
            throw IOException("${profile.displayName} CLI not executable: ${paths.cliPath}")
        }

        val cmd = buildCommand(paths.cliPath, projectPath, filePath, line, column)
        val process = ProcessBuilder(cmd.toList())
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
