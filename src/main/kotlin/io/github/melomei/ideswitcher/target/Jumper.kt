package io.github.melomei.ideswitcher.target

import java.io.File
import java.io.IOException

interface Jumper {
    val target: Target
    val displayName: String
    val appPath: String
    val cliPath: String

    fun isInstalled(): Boolean =
        File(appPath).let { it.exists() && it.isDirectory }

    @Throws(IOException::class)
    fun jump(projectPath: String, filePath: String?, line: Int?, column: Int?) {
        val cli = File(cliPath)
        if (!cli.canExecute()) {
            throw IOException("$displayName CLI not found or not executable: $cliPath")
        }
        val cmd = buildCommand(projectPath, filePath, line, column)
        ProcessBuilder(cmd.toList())
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
            .outputStream.close()
    }

    fun buildCommand(
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
