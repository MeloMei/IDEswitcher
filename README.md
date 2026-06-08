# IDEswitcher

<p align="center">
  <img src="https://img.shields.io/badge/version-1.2.0-blue" alt="Version">
  <img src="https://img.shields.io/badge/platform-macOS-000000" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%2B%20TypeScript-0095D5" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License">
</p>

In the era of agentic coding, running IntelliJ IDEA alongside an AI-powered coding platform has become everyday practice.

**IDEswitcher** provides **seamless bidirectional jumping** between IntelliJ IDEA and Qoder / CodeFuse, precisely preserving file path and cursor position (line + column).

### Features

- **Bidirectional Jump**: IDEA ↔ Qoder / IDEA ↔ CodeFuse, unified shortcut `⌥⇧O`
- **Precise Positioning**: Cursor lands on the exact same line and column after jumping
- **Configurable Target**: Choose Qoder or CodeFuse in Settings → Tools → IDEswitcher
- **Smart Defaults**: Auto-detects installed editors on first launch

---

## Quick Start

### 1. Install the IntelliJ IDEA Plugin

**Option A: Direct Install**

Download `IDEswitcher-1.2.0.zip` from [Releases](../../releases), then in IDEA: Settings → Plugins → ⚙️ → Install Plugin from Disk...

**Option B: Build from Source**

```bash
cd IDEswitcher-main
./gradlew build
# Output: build/distributions/IDEswitcher-1.2.0.zip
```

After installation, select your jump target (Qoder or CodeFuse) in Settings → Tools → IDEswitcher.

### 2. Install the Editor Extension (Qoder / CodeFuse)

```bash
cd IDEswitcher-main/agentic-ide-extension
npm install && npm run compile
```

Copy the entire `agentic-ide-extension/` directory to the corresponding location:

| Editor   | Install Path                   | Directory Name                      |
|----------|--------------------------------|-------------------------------------|
| Qoder    | `~/.qoder/extensions/`         | `ide-switcher`                      |
| CodeFuse | `~/.codefuse/extensions/`      | `meloluvmei.ide-switcher-1.2.0`   |

> **Note**: CodeFuse requires the extension directory name to follow the `publisher.name-version` format.

### 3. Usage

Press `⌥⇧O` (Option+Shift+O) in either IDE to jump to the other IDE at the same file and cursor position.

You can also trigger it via the context menu → **Jump to Editor** / **Jump to IntelliJ IDEA**.

---

## How It Works

```
IDEA → Qoder/CodeFuse:  ⌥⇧O → Read Settings.target → code --goto file:line:col
Qoder/CodeFuse → IDEA:  ⌥⇧O → Detect IDEA version → idea --line L --column C file
```

### Project Structure

```
IDEswitcher-main/
├── src/main/kotlin/io/github/meloluvmei/ideswitcher/
│   ├── action/JumpAction.kt              # IDEA main Action
│   ├── target/
│   │   ├── Target.kt                     # Enum: QODER / CODEFUSE
│   │   ├── Jumper.kt                     # Jump interface (with default impl)
│   │   ├── QoderJumper.kt               # Qoder jump constants
│   │   └── CodeFuseJumper.kt            # CodeFuse jump constants
│   └── settings/
│       ├── IdeSwitcherSettings.kt        # Persistent configuration
│       └── IdeSwitcherConfigurable.kt    # Settings page UI
├── src/main/resources/META-INF/
│   └── plugin.xml                        # IntelliJ plugin descriptor
├── src/test/                             # Unit tests
├── agentic-ide-extension/                # VS Code extension (universal)
│   ├── src/extension.ts                  # Core logic
│   └── out/extension.js                  # Compiled output
└── build.gradle.kts                      # Gradle build config
```

---

## Development

```bash
cd IDEswitcher-main

# Launch sandbox IDE (plugin auto-loaded)
./gradlew runIde

# Run tests
./gradlew test

# Clean
./gradlew clean
```

### Tech Stack

- **IDEA Plugin**: Kotlin 1.9 + IntelliJ Platform SDK 2024.1
- **Editor Extension**: TypeScript + VS Code Extension API
- **Build**: Gradle (IDEA) / npm (extension)
- **No runtime external dependencies**

---

## Known Limitations

- macOS only (contributions for Linux/Windows support are welcome!)
- IntelliJ IDEA path detection only covers standard installations under `/Applications`; JetBrains Toolbox custom paths are not auto-detected
- Qoder / CodeFuse paths are hardcoded to `/Applications/Qoder.app` and `/Applications/CodeFuse.app`

## Contributing

Bug reports and pull requests are welcome! Feel free to open an [issue](../../issues) or submit a PR.

---

## License

[MIT](LICENSE)
