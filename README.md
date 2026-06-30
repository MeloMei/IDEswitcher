# IDEswitcher

<p align="center">
  <img src="https://img.shields.io/badge/version-1.7.0-blue" alt="Version">
  <img src="https://img.shields.io/badge/platform-macOS%20%7C%20Windows%20%7C%20Linux-000000" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%2B%20TypeScript-0095D5" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License">
</p>

In the era of agentic coding, running IntelliJ IDEA alongside an AI-powered coding platform has become everyday practice.

**IDEswitcher** provides **seamless bidirectional jumping** between IntelliJ IDEA and Qoder / CodeFuse / Cursor / Windsurf / Trae, precisely preserving file path and cursor position (line + column).

### Features

- **Bidirectional Jump**: IDEA ↔ AI Editor, unified shortcut `⌥⇧O` (Mac) / `Ctrl+Alt+O` (Windows/Linux)
- **Precise Positioning**: Cursor lands on the exact same line and column after jumping
- **5 Editor Targets**: Qoder, CodeFuse, Cursor, Windsurf, Trae — configurable in Settings → Tools → IDEswitcher
- **Smart Defaults**: Auto-detects installed editors on first launch
- **Status Bar Widget**: Shows current jump target — click to cycle through installed editors

---

## Quick Start

### 1. Install the IntelliJ IDEA Plugin

**Option A: Direct Install**

Download `IDEswitcher-1.7.0.zip` from [Releases](../../releases), then in IDEA: Settings → Plugins → ⚙️ → Install Plugin from Disk...

**Option B: Build from Source**

```bash
cd IDEswitcher-main
./gradlew build
# Output: build/distributions/IDEswitcher-1.7.0.zip
```

After installation, select your jump target in Settings → Tools → IDEswitcher.

### 2. Install the Editor Extension (Qoder / CodeFuse / Cursor / Windsurf / Trae)

```bash
cd IDEswitcher-main/agentic-ide-extension
npm install && npm run compile
```

Copy the entire `agentic-ide-extension/` directory to the corresponding location:

| Editor    | Install Path                   | Directory Name                       |
|-----------|--------------------------------|--------------------------------------|
| Qoder     | `~/.qoder/extensions/`         | `ide-switcher`                       |
| CodeFuse  | `~/.codefuse/extensions/`      | `melomei.ide-switcher-1.7.0`    |
| Cursor    | Built-in (VS Code extension)   | Install via Extensions panel         |
| Windsurf  | Built-in (VS Code extension)   | Install via Extensions panel         |
| Trae      | Built-in (VS Code extension)   | Install via Extensions panel         |

> **Note**: CodeFuse requires the extension directory name to follow the `publisher.name-version` format.
> **Note**: Cursor, Windsurf, and Trae are VS Code forks — the extension can be installed via their built-in extension marketplace or by copying the folder to their extensions directory.

### 3. Usage

Press `⌥⇧O` (Option+Shift+O) on Mac or `Ctrl+Alt+O` on Windows/Linux in either IDE to jump to the other IDE at the same file and cursor position.

You can also trigger it via the context menu → **Jump to Editor** / **Jump to IntelliJ IDEA**.

---

## How It Works

```
IDEA → AI Editor:  shortcut → Read Settings.target → EditorProfile.resolvePath() → code --goto file:line:col
AI Editor → IDEA:  shortcut → detectIntelliJ() [PATH | /Applications | Toolbox] → idea --line L --column C file
```

### Project Structure

```
IDEswitcher-main/
├── src/main/kotlin/io/github/melomei/ideswitcher/
│   ├── action/JumpAction.kt              # IDEA main Action
│   ├── platform/Platform.kt              # OS detection enum
│   ├── target/
│   │   ├── Target.kt                     # Enum: QODER / CODEFUSE / CURSOR / WINDSURF / TRAE
│   │   ├── EditorProfile.kt             # Cross-platform path resolution
│   │   ├── Jumper.kt                     # Jump interface (with default impl)
│   │   ├── QoderJumper.kt               # Qoder jump constants
│   │   ├── CodeFuseJumper.kt            # CodeFuse jump constants
│   │   ├── CursorJumper.kt              # Cursor jump constants
│   │   ├── WindsurfJumper.kt            # Windsurf jump constants
│   │   └── TraeJumper.kt                # Trae jump constants
│   └── settings/
│       ├── IdeSwitcherSettings.kt        # Persistent configuration
│       └── IdeSwitcherConfigurable.kt    # Settings page UI
├── src/main/resources/META-INF/
│   └── plugin.xml                        # IntelliJ plugin descriptor
├── src/test/                             # Unit tests
├── agentic-ide-extension/                # VS Code extension (universal)
│   ├── src/
│   │   ├── extension.ts                  # Core logic
│   │   ├── detect.ts                     # Cross-platform IntelliJ detection
│   │   └── detect.test.ts                # Detection tests
│   └── out/extension.js                  # Compiled output
└── build.gradle.kts                      # Gradle build config
```

---

## Development

```bash
cd IDEswitcher-main

# Launch sandbox IDE (plugin auto-loaded)
./gradlew runIde

# Run Kotlin tests
./gradlew test

# Run TypeScript tests
cd agentic-ide-extension && npm test

# Clean
./gradlew clean
```

### Tech Stack

- **IDEA Plugin**: Kotlin 1.9 + IntelliJ Platform SDK 2024.1
- **Editor Extension**: TypeScript + VS Code Extension API
- **Build**: Gradle (IDEA) / npm + vitest (extension)
- **No runtime external dependencies**

---

## Known Limitations

- Supports macOS, Windows, and Linux
- Editor app paths default to standard installation locations — use the custom path setting for non-standard installations
- Linux: Flatpak installations are not auto-detected; use the custom path setting instead

## Troubleshooting

**"Editor not found" balloon notification:**

1. Make sure the target editor is installed
2. If installed in a non-standard location, set a custom path in Settings → Tools → IDEswitcher
3. For JetBrains Toolbox users: the plugin now auto-detects Toolbox-managed installations

**Jump fails silently or CLI errors:**

The plugin now captures stderr and shows it in a balloon notification. Common causes:

- CLI binary missing: some editors require you to install their CLI manually (e.g., VS Code → "Install 'code' command in PATH")
- Permission denied (macOS): run `chmod +x <path-to-cli>` on the CLI binary
- Permission denied (Windows): try running your editor as Administrator
- Permission denied (Linux): run `chmod +x <path-to-cli>` or ensure the editor is installed via your package manager

**Extension side (jumping back to IntelliJ):**

- "IntelliJ IDEA CLI not found" (macOS): Open IntelliJ → Tools → Create Command-line Launcher
- "IntelliJ IDEA CLI not found" (Windows): Make sure IntelliJ is installed and `idea64.exe` is on your PATH
- "IntelliJ IDEA CLI not found" (Linux): Install via snap (`sudo snap install intellij-idea-ultimate --classic`) or add the `bin/` directory to your PATH
- "IntelliJ IDEA took too long to respond": IDEA may be busy indexing; wait and try again

---

## Contributing

Bug reports and pull requests are welcome! Feel free to open an [issue](../../issues) or submit a PR.

---

## License

[MIT](LICENSE)
