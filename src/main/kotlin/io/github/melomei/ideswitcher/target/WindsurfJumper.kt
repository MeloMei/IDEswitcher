package io.github.melomei.ideswitcher.target

object WindsurfJumper : Jumper {
    override val target = Target.WINDSURF
    override val displayName = "Windsurf"
    override val appPath = "/Applications/Windsurf.app"
    override val cliPath = "$appPath/Contents/Resources/app/bin/code"
}
