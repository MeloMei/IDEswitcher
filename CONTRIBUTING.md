# Contributing to IDEswitcher

Thank you for your interest in contributing! This guide covers how to add new editor support, report bugs, and submit pull requests.

## Adding a New Editor

IDEswitcher supports an extension point that allows third-party plugins to register new editor jumpers. You can either:

1. **Submit a PR to add it built-in** (for popular editors)
2. **Create your own plugin** that registers via the extension point

### Option 1: Built-in Editor (PR to this repo)

**Step 1: Add Target enum value**

In `src/main/kotlin/.../target/Target.kt`:

```kotlin
enum class Target(val displayName: String) {
    // ... existing entries
    MY_EDITOR("My Editor")
}
```

**Step 2: Add EditorProfile entry**

In `src/main/kotlin/.../target/EditorProfile.kt`, add a new enum value with platform paths:

```kotlin
MY_EDITOR(
    target = Target.MY_EDITOR, displayName = "My Editor",
    macPath = PlatformPath("/Applications/MyEditor.app", "{appDir}/Contents/Resources/app/bin/code", listOf("myeditor", "code")),
    windowsPath = PlatformPath("%LOCALAPPDATA%\Programs\MyEditor", "{appDir}in\code.cmd", listOf("code.cmd")),
    linuxPath = PlatformPath("/opt/myeditor", "{appDir}/bin/code", listOf("myeditor", "code")),
),
```

**Step 3: Create Jumper object**

```kotlin
object MyEditorJumper : Jumper {
    override val profile = EditorProfile.MY_EDITOR
}
```

**Step 4: Register in JumpAction**

Add the new target to the `when` expression in `JumpAction.kt`.

**Step 5: Update Settings UI**

Add a radio button in `IdeSwitcherConfigurable.kt`.

**Step 6: Add tests**

- Update `JumperTest.kt` with path verification tests
- Ensure `buildCommand` works correctly

### Option 2: Extension Point Plugin

Create your own IntelliJ plugin that depends on IDEswitcher and registers a `JumperProvider`:

**1. Add IDEswitcher as a dependency** in your `build.gradle.kts`:

```kotlin
intellij {
    plugins.set(listOf("io.github.melomei.ideswitcher:1.7.0"))
}
```

**2. Implement `JumperProvider`**:

```kotlin
class MyEditorProvider : JumperProvider {
    override val id = "my-editor"
    override val displayName = "My Editor"
    override fun createJumper(): Jumper = MyEditorJumper
}
```

**3. Register in your `plugin.xml`**:

```xml
<extensions defaultExtensionNs="io.github.melomei.ideswitcher">
    <jumper implementation="com.example.MyEditorProvider"/>
</extensions>
```

## Reporting Bugs

Use the [bug report template](../../issues/new?template=bug_report.md). Include:

- IDEswitcher version
- IntelliJ IDEA version and OS
- Steps to reproduce
- Expected vs actual behavior
- Any error messages from the Event Log

## Suggesting Features

Use the [feature request template](../../issues/new?template=feature_request.md). Explain:

- The problem you are trying to solve
- Your proposed solution
- Any alternatives you have considered

## Development Setup

```bash
git clone https://github.com/MeloMei/IDEswitcher.git
cd IDEswitcher

# Run sandbox IDE with plugin loaded
./gradlew runIde

# Run all tests
./gradlew test
cd agentic-ide-extension && npm install && npm test
```

## Pull Request Process

1. Fork the repo and create a feature branch
2. Make your changes with tests
3. Ensure `./gradlew test` and `npm test` both pass
4. Submit a PR with a clear description
5. Wait for review

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
