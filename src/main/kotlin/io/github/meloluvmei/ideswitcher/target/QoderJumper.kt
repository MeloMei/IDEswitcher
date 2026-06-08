package io.github.meloluvmei.ideswitcher.target

object QoderJumper : Jumper {
    override val target = Target.QODER
    override val displayName = "Qoder"
    override val appPath = "/Applications/Qoder.app"
    override val cliPath = "$appPath/Contents/Resources/app/bin/code"
}
