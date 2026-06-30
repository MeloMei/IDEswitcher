package io.github.melomei.ideswitcher.target

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    // --- Jumper path and target verification ---

    @Test
    fun `QoderJumper has correct paths and target`() {
        assertEquals(Target.QODER, QoderJumper.target)
        assertEquals("Qoder", QoderJumper.displayName)
        assertEquals("/Applications/Qoder.app", QoderJumper.appPath)
        assertTrue(QoderJumper.cliPath.startsWith(QoderJumper.appPath))
        assertTrue(QoderJumper.cliPath.endsWith("/code"))
    }

    @Test
    fun `CodeFuseJumper has correct paths and target`() {
        assertEquals(Target.CODEFUSE, CodeFuseJumper.target)
        assertEquals("CodeFuse", CodeFuseJumper.displayName)
        assertEquals("/Applications/CodeFuse.app", CodeFuseJumper.appPath)
        assertTrue(CodeFuseJumper.cliPath.startsWith(CodeFuseJumper.appPath))
        assertTrue(CodeFuseJumper.cliPath.endsWith("/code"))
    }

    @Test
    fun `CursorJumper has correct paths and target`() {
        assertEquals(Target.CURSOR, CursorJumper.target)
        assertEquals("Cursor", CursorJumper.displayName)
        assertEquals("/Applications/Cursor.app", CursorJumper.appPath)
        assertTrue(CursorJumper.cliPath.startsWith(CursorJumper.appPath))
        assertTrue(CursorJumper.cliPath.endsWith("/code"))
    }

    @Test
    fun `WindsurfJumper has correct paths and target`() {
        assertEquals(Target.WINDSURF, WindsurfJumper.target)
        assertEquals("Windsurf", WindsurfJumper.displayName)
        assertEquals("/Applications/Windsurf.app", WindsurfJumper.appPath)
        assertTrue(WindsurfJumper.cliPath.startsWith(WindsurfJumper.appPath))
        assertTrue(WindsurfJumper.cliPath.endsWith("/code"))
    }

    @Test
    fun `TraeJumper has correct paths and target`() {
        assertEquals(Target.TRAE, TraeJumper.target)
        assertEquals("Trae", TraeJumper.displayName)
        assertEquals("/Applications/Trae.app", TraeJumper.appPath)
        assertTrue(TraeJumper.cliPath.startsWith(TraeJumper.appPath))
        assertTrue(TraeJumper.cliPath.endsWith("/code"))
    }

    @Test
    fun `all jumpers produce correct --goto command format`() {
        val jumpers = listOf(QoderJumper, CodeFuseJumper, CursorJumper, WindsurfJumper, TraeJumper)
        for (jumper in jumpers) {
            val cmd = jumper.buildCommand("/proj", "/proj/Main.kt", 10, 5)
            assertEquals(jumper.cliPath, cmd[0], "${jumper.displayName} cliPath mismatch")
            assertEquals("--goto", cmd[1], "${jumper.displayName} --goto flag missing")
            assertEquals("/proj/Main.kt:10:5", cmd[2], "${jumper.displayName} goto arg wrong")
        }
    }
}
