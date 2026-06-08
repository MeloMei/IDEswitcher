package io.github.melomei.ideswitcher.target

import kotlin.test.Test
import kotlin.test.assertContentEquals

class JumperTest {

    private object FakeJumper : Jumper {
        override val target = Target.QODER
        override val displayName = "Fake"
        override val appPath = "/tmp/Fake.app"
        override val cliPath = "/tmp/Fake.app/bin/code"
    }

    @Test
    fun `buildCommand with file + line + column uses --goto`() {
        val cmd = FakeJumper.buildCommand(
            projectPath = "/proj",
            filePath = "/proj/src/Foo.kt",
            line = 42,
            column = 7
        )
        assertContentEquals(
            arrayOf("/tmp/Fake.app/bin/code", "--goto", "/proj/src/Foo.kt:42:7"),
            cmd
        )
    }

    @Test
    fun `buildCommand with file + line but no column defaults column to 1`() {
        val cmd = FakeJumper.buildCommand(
            projectPath = "/proj",
            filePath = "/proj/src/Foo.kt",
            line = 42,
            column = null
        )
        assertContentEquals(
            arrayOf("/tmp/Fake.app/bin/code", "--goto", "/proj/src/Foo.kt:42:1"),
            cmd
        )
    }

    @Test
    fun `buildCommand with file but no line opens file directly`() {
        val cmd = FakeJumper.buildCommand(
            projectPath = "/proj",
            filePath = "/proj/src/Foo.kt",
            line = null,
            column = null
        )
        assertContentEquals(
            arrayOf("/tmp/Fake.app/bin/code", "/proj/src/Foo.kt"),
            cmd
        )
    }

    @Test
    fun `buildCommand without file opens project root`() {
        val cmd = FakeJumper.buildCommand(
            projectPath = "/proj",
            filePath = null,
            line = null,
            column = null
        )
        assertContentEquals(
            arrayOf("/tmp/Fake.app/bin/code", "/proj"),
            cmd
        )
    }

    @Test
    fun `buildCommand ignores line when filePath is null`() {
        val cmd = FakeJumper.buildCommand(
            projectPath = "/proj",
            filePath = null,
            line = 99,
            column = 99
        )
        assertContentEquals(
            arrayOf("/tmp/Fake.app/bin/code", "/proj"),
            cmd
        )
    }
}
