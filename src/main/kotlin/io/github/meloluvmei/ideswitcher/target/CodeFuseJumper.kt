package io.github.meloluvmei.ideswitcher.target

object CodeFuseJumper : Jumper {
    override val target = Target.CODEFUSE
    override val displayName = "CodeFuse"
    override val appPath = "/Applications/CodeFuse.app"
    override val cliPath = "$appPath/Contents/Resources/app/bin/code"
}
