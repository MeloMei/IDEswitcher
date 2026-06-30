package io.github.melomei.ideswitcher.target

object TraeJumper : Jumper {
    override val target = Target.TRAE
    override val displayName = "Trae"
    override val appPath = "/Applications/Trae.app"
    override val cliPath = "$appPath/Contents/Resources/app/bin/code"
}
