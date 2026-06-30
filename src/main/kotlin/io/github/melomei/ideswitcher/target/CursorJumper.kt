package io.github.melomei.ideswitcher.target

object CursorJumper : Jumper {
    override val target = Target.CURSOR
    override val displayName = "Cursor"
    override val appPath = "/Applications/Cursor.app"
    override val cliPath = "$appPath/Contents/Resources/app/bin/code"
}
