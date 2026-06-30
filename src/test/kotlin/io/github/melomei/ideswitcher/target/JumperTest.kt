package io.github.melomei.ideswitcher.target

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JumperTest {

    @Test
    fun `buildCommand with file + line + column uses --goto`() {
        val cli = "/tmp/Fake.app/bin/code"
        val cmd = QoderJumper.buildCommand(cli, "/proj", "/proj/src/Foo.kt", 42, 7)
        assertContentEquals(
            arrayOf(cli, "--goto", "/proj/src/Foo.kt:42:7"),
            cmd
        )
    }

    @Test
    fun `buildCommand with file + line but no column defaults column to 1`() {
        val cli = "/tmp/Fake.app/bin/code"
        val cmd = QoderJumper.buildCommand(cli, "/proj", "/proj/src/Foo.kt", 42, null)
        assertContentEquals(
            arrayOf(cli, "--goto", "/proj/src/Foo.kt:42:1"),
            cmd
        )
    }

    @Test
    fun `buildCommand with file but no line opens file directly`() {
        val cli = "/tmp/Fake.app/bin/code"
        val cmd = QoderJumper.buildCommand(cli, "/proj", "/proj/src/Foo.kt", null, null)
        assertContentEquals(
            arrayOf(cli, "/proj/src/Foo.kt"),
            cmd
        )
    }

    @Test
    fun `buildCommand without file opens project root`() {
        val cli = "/tmp/Fake.app/bin/code"
        val cmd = QoderJumper.buildCommand(cli, "/proj", null, null, null)
        assertContentEquals(
            arrayOf(cli, "/proj"),
            cmd
        )
    }

    @Test
    fun `buildCommand ignores line when filePath is null`() {
        val cli = "/tmp/Fake.app/bin/code"
        val cmd = QoderJumper.buildCommand(cli, "/proj", null, 99, 99)
        assertContentEquals(
            arrayOf(cli, "/proj"),
            cmd
        )
    }

    // --- EditorProfile verification ---

    @Test
    fun `all profiles have correct target mapping`() {
        assertEquals(Target.QODER, EditorProfile.QODER.target)
        assertEquals(Target.CODEFUSE, EditorProfile.CODEFUSE.target)
        assertEquals(Target.CURSOR, EditorProfile.CURSOR.target)
        assertEquals(Target.WINDSURF, EditorProfile.WINDSURF.target)
        assertEquals(Target.TRAE, EditorProfile.TRAE.target)
    }

    @Test
    fun `all profiles have display names`() {
        assertEquals("Qoder", EditorProfile.QODER.displayName)
        assertEquals("CodeFuse", EditorProfile.CODEFUSE.displayName)
        assertEquals("Cursor", EditorProfile.CURSOR.displayName)
        assertEquals("Windsurf", EditorProfile.WINDSURF.displayName)
        assertEquals("Trae", EditorProfile.TRAE.displayName)
    }

    @Test
    fun `all profiles have macOS app dirs ending in dot app`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.macPath.appDir.endsWith(".app"),
                "${profile.displayName} macPath.appDir should end with .app"
            )
        }
    }

    @Test
    fun `all profiles have macOS cli paths ending in code`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.macPath.cliPath.endsWith("/code"),
                "${profile.displayName} macPath.cliPath should end with /code"
            )
        }
    }

    @Test
    fun `all profiles have Windows cli paths ending in code dot cmd`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.windowsPath.cliPath.endsWith("code.cmd"),
                "${profile.displayName} windowsPath.cliPath should end with code.cmd"
            )
        }
    }

    @Test
    fun `all profiles use LOCALAPPDATA in Windows paths`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.windowsPath.appDir.contains("LOCALAPPDATA"),
                "${profile.displayName} windowsPath.appDir should reference %LOCALAPPDATA%"
            )
        }
    }

    @Test
    fun `all profiles have Linux app dirs under opt`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.linuxPath.appDir.startsWith("/opt/"),
                "${profile.displayName} linuxPath.appDir should start with /opt/"
            )
        }
    }

    @Test
    fun `all profiles have Linux cli binary names list`() {
        for (profile in EditorProfile.entries) {
            assertTrue(
                profile.linuxPath.cliBinaryNames.isNotEmpty(),
                "${profile.displayName} linuxPath.cliBinaryNames should not be empty"
            )
            // The editor-specific binary name should be first, 'code' as fallback
            assertTrue(
                profile.linuxPath.cliBinaryNames.last() == "code",
                "${profile.displayName} linuxPath.cliBinaryNames should end with 'code' as fallback"
            )
        }
    }

    @Test
    fun `all platforms have non-empty cliBinaryNames`() {
        for (profile in EditorProfile.entries) {
            assertTrue(profile.macPath.cliBinaryNames.isNotEmpty())
            assertTrue(profile.windowsPath.cliBinaryNames.isNotEmpty())
            assertTrue(profile.linuxPath.cliBinaryNames.isNotEmpty())
        }
    }

    @Test
    fun `forTarget returns correct profile for each target`() {
        for (target in Target.entries) {
            val profile = EditorProfile.forTarget(target)
            assertEquals(target, profile.target)
        }
    }

    @Test
    fun `all jumpers have matching profile`() {
        assertEquals(EditorProfile.QODER, QoderJumper.profile)
        assertEquals(EditorProfile.CODEFUSE, CodeFuseJumper.profile)
        assertEquals(EditorProfile.CURSOR, CursorJumper.profile)
        assertEquals(EditorProfile.WINDSURF, WindsurfJumper.profile)
        assertEquals(EditorProfile.TRAE, TraeJumper.profile)
    }

    @Test
    fun `all jumpers produce correct --goto command format`() {
        val jumpers = listOf(QoderJumper, CodeFuseJumper, CursorJumper, WindsurfJumper, TraeJumper)
        for (jumper in jumpers) {
            val cli = jumper.profile.macPath.cliPath
            val cmd = jumper.buildCommand(cli, "/proj", "/proj/Main.kt", 10, 5)
            assertEquals(cli, cmd[0], "${jumper.profile.displayName} cliPath mismatch")
            assertEquals("--goto", cmd[1], "${jumper.profile.displayName} --goto flag missing")
            assertEquals("/proj/Main.kt:10:5", cmd[2], "${jumper.profile.displayName} goto arg wrong")
        }
    }
}
