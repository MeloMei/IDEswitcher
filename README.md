# IDEswitcher

<p align="center">
  <img src="https://img.shields.io/badge/version-2.0.0-blue" alt="Version">
  <img src="https://img.shields.io/badge/platform-macOS%20%7C%20Windows%20%7C%20Linux-000000" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%2B%20TypeScript-0095D5" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License">
  <a href="README_zh.md"><img src="https://img.shields.io/badge/%E4%B8%AD%E6%96%87-README-red" alt="Chinese"></a>
</p>

In the era of agentic coding, running IntelliJ IDEA alongside an AI-powered coding platform has become everyday practice.

**IDEswitcher** provides **seamless bidirectional jumping** between IntelliJ IDEA and AI editors (Qoder, CodeFuse, Cursor, Windsurf, Trae), precisely preserving file path and cursor position (line + column).

### Features

- **Bidirectional Jump**: IDEA <-> AI Editor with `Option+Shift+O` (Mac) / `Ctrl+Alt+O` (Windows/Linux)
- **Precise Positioning**: Cursor lands on the exact same line and column after jumping
- **5 Editor Targets**: Qoder, CodeFuse, Cursor, Windsurf, Trae -- extensible via plugin API
- **Smart Path Detection**: Auto-discovers editors via `$PATH`, `/Applications`, `Program Files`, Snap, JetBrains Toolbox
- **Custom Path Override**: Configure non-standard installations via Settings UI
- **Status Bar Widget**: Shows current jump target -- click to cycle through installed editors
- **Balloon Notifications**: Jump failures show actionable error messages with [Open Settings] button
- **Cross-Platform**: macOS, Windows, and Linux

---

## Quick Start

### 1. Install the IntelliJ Plugin

**Option A: From JetBrains Marketplace** (coming soon)

Search "IDEswitcher" in Settings -> Plugins -> Marketplace.

**Option B: Build from Source**

```bash
git clone https://github.com/MeloMei/IDEswitcher.git
cd IDEswitcher
./gradlew buildPlugin
# Output: build/distributions/IDEswitcher-2.0.0.zip
```

Install via: Settings -> Plugins -> gear icon -> Install Plugin from Disk...

### 2. Install the Editor Extension

```bash
cd agentic-ide-extension
npm install && npm run compile
```

Copy the `agentic-ide-extension/` directory to:

| Editor    | Install Path                   | Directory Name                       |
|-----------|--------------------------------|--------------------------------------|
| Qoder     | `~/.qoder/extensions/`         | `ide-switcher`                       |
| CodeFuse  | `~/.codefuse/extensions/`      | `melomei.ide-switcher-2.0.0`         |
| Cursor    | Via Extensions panel           | Search "IDEswitcher"                 |
| Windsurf  | Via Extensions panel           | Search "IDEswitcher"                 |
| Trae      | Via Extensions panel           | Search "IDEswitcher"                 |

> **Note**: CodeFuse requires the `publisher.name-version` directory naming format.

### 3. Usage

Press the shortcut in either IDE:

| Platform | Shortcut |
|----------|----------|
| macOS    | `Option+Shift+O` |
| Windows/Linux | `Ctrl+Alt+O` |

You can also use the context menu: **Jump to Editor** / **Jump to IntelliJ IDEA**.

---

## How It Works

```
IDEA -> AI Editor:  shortcut -> EditorProfile.resolvePath() -> code --goto file:line:col
AI Editor -> IDEA:  shortcut -> detectIntelliJ() [$PATH | /Applications | Toolbox] -> idea --line L --column C file
```

### Architecture

```
IDEswitcher/
+-- src/main/kotlin/.../ideswitcher/
|   +-- action/JumpAction.kt              # Main jump action
|   +-- platform/Platform.kt              # OS detection (MACOS/WINDOWS/LINUX)
|   +-- target/
|   |   +-- Target.kt                     # Editor enum
|   |   +-- EditorProfile.kt             # Cross-platform path resolution
|   |   +-- Jumper.kt                     # Jump interface
|   |   +-- QoderJumper.kt ... TraeJumper.kt
|   +-- settings/
|   |   +-- IdeSwitcherSettings.kt        # Persistent state
|   |   +-- IdeSwitcherConfigurable.kt    # Settings UI (table + radio)
|   +-- widget/TargetWidget.kt            # Status bar widget
|   +-- startup/WelcomeActivity.kt        # First-run notification
|   +-- extension/JumperProvider.kt       # Third-party extension point
+-- agentic-ide-extension/                # VS Code extension
|   +-- src/extension.ts                  # Core logic
|   +-- src/detect.ts                     # Cross-platform IntelliJ detection
+-- .github/workflows/build.yml           # CI
+-- CONTRIBUTING.md                       # How to add new editors
```

---

## Extending IDEswitcher

Third-party plugins can register custom editor jumpers via the `JumperProvider` extension point. See [CONTRIBUTING.md](CONTRIBUTING.md) for full instructions.

```kotlin
class MyEditorProvider : JumperProvider {
    override val id = "my-editor"
    override val displayName = "My Editor"
    override fun createJumper(): Jumper = MyEditorJumper
}
```

Register in your `plugin.xml`:

```xml
<extensions defaultExtensionNs="io.github.melomei.ideswitcher">
    <jumper implementation="com.example.MyEditorProvider"/>
</extensions>
```

---

## Development

```bash
# Launch sandbox IDE
./gradlew runIde

# Kotlin tests (17 tests)
./gradlew test

# TypeScript tests (8 tests)
cd agentic-ide-extension && npm test
```

### Tech Stack

- **IDEA Plugin**: Kotlin 1.9 + IntelliJ Platform SDK 2024.1
- **Editor Extension**: TypeScript + VS Code Extension API
- **Build**: Gradle (IDEA) / npm + vitest (extension)
- **CI**: GitHub Actions (macOS + Windows + Linux matrix)
- **No runtime external dependencies**

---

## Troubleshooting

**"Editor not found" notification:**

1. Make sure the target editor is installed
2. If in a non-standard location, set a custom path in Settings -> Tools -> IDEswitcher
3. JetBrains Toolbox users: the plugin auto-detects Toolbox installations

**Jump fails with CLI error:**

- CLI binary missing: install the CLI (e.g., VS Code -> "Install 'code' command in PATH")
- Permission denied (macOS/Linux): `chmod +x <path-to-cli>`
- Permission denied (Windows): try running as Administrator

**Extension side (jumping back to IntelliJ):**

- "CLI not found" (macOS): IntelliJ -> Tools -> Create Command-line Launcher
- "CLI not found" (Windows): ensure `idea64.exe` is on your PATH
- "CLI not found" (Linux): `sudo snap install intellij-idea-ultimate --classic` or add `bin/` to PATH

---

## Contributing

Bug reports and pull requests are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for details on adding new editors, reporting bugs, and submitting PRs.

## License

[MIT](LICENSE)
