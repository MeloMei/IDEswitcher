# IDEswitcher

<p align="center">
  <img src="https://img.shields.io/badge/version-2.0.0-blue" alt="Version">
  <img src="https://img.shields.io/badge/platform-macOS%20%7C%20Windows%20%7C%20Linux-000000" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin%20%2B%20TypeScript-0095D5" alt="Language">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="License">
  <a href="README.md"><img src="https://img.shields.io/badge/English-README-blue" alt="English"></a>
</p>

在 AI 编程时代，IntelliJ IDEA 搭配 AI 编辑器已成为日常开发的标准配置。

**IDEswitcher** 提供 IntelliJ IDEA 与 AI 编辑器（Qoder、CodeFuse、Cursor、Windsurf、Trae）之间的**无缝双向跳转**，精确保持文件路径和光标位置（行 + 列）。

### 功能特性

- **双向跳转**：IDEA <-> AI 编辑器，快捷键 `Option+Shift+O`（Mac）/ `Ctrl+Alt+O`（Windows/Linux）
- **精确定位**：跳转后光标落在完全相同的行和列
- **5 个编辑器目标**：Qoder、CodeFuse、Cursor、Windsurf、Trae -- 支持通过插件 API 扩展
- **智能路径探测**：自动通过 `$PATH`、`/Applications`、`Program Files`、Snap、JetBrains Toolbox 发现编辑器
- **自定义路径覆盖**：通过 Settings UI 配置非标准安装路径
- **状态栏 Widget**：显示当前跳转目标 -- 点击可在已安装的编辑器间切换
- **气球通知**：跳转失败时显示可操作的错误信息，附带 [Open Settings] 按钮
- **跨平台**：macOS、Windows 和 Linux

---

## 快速开始

### 1. 安装 IntelliJ 插件

**方式 A：从 JetBrains Marketplace 安装**（即将上架）

在 Settings -> Plugins -> Marketplace 中搜索 "IDEswitcher"。

**方式 B：从源码构建**

```bash
git clone https://github.com/MeloMei/IDEswitcher.git
cd IDEswitcher
./gradlew buildPlugin
# 输出：build/distributions/IDEswitcher-2.0.0.zip
```

通过 Settings -> Plugins -> 齿轮图标 -> Install Plugin from Disk... 安装。

### 2. 安装编辑器扩展

```bash
cd agentic-ide-extension
npm install && npm run compile
```

将 `agentic-ide-extension/` 目录复制到对应位置：

| 编辑器    | 安装路径                     | 目录名                           |
|-----------|------------------------------|----------------------------------|
| Qoder     | `~/.qoder/extensions/`       | `ide-switcher`                   |
| CodeFuse  | `~/.codefuse/extensions/`    | `melomei.ide-switcher-2.0.0`     |
| Cursor    | 通过 Extensions 面板          | 搜索 "IDEswitcher"               |
| Windsurf  | 通过 Extensions 面板          | 搜索 "IDEswitcher"               |
| Trae      | 通过 Extensions 面板          | 搜索 "IDEswitcher"               |

> **注意**：CodeFuse 要求目录名遵循 `publisher.name-version` 格式。

### 3. 使用方法

在任一 IDE 中按下快捷键：

| 平台           | 快捷键             |
|----------------|--------------------|
| macOS          | `Option+Shift+O`   |
| Windows/Linux  | `Ctrl+Alt+O`       |

也可以通过右键菜单：**Jump to Editor** / **Jump to IntelliJ IDEA**。

---

## 工作原理

```
IDEA -> AI 编辑器：快捷键 -> EditorProfile.resolvePath() -> code --goto file:line:col
AI 编辑器 -> IDEA：快捷键 -> detectIntelliJ() [$PATH | /Applications | Toolbox] -> idea --line L --column C file
```

### 项目结构

```
IDEswitcher/
|-- src/main/kotlin/.../ideswitcher/
|   |-- action/JumpAction.kt              # 主跳转 Action
|   |-- platform/Platform.kt              # 操作系统检测（MACOS/WINDOWS/LINUX）
|   |-- target/
|   |   |-- Target.kt                     # 编辑器枚举
|   |   |-- EditorProfile.kt             # 跨平台路径解析
|   |   |-- Jumper.kt                     # 跳转接口
|   |   |-- QoderJumper.kt ... TraeJumper.kt
|   |-- settings/
|   |   |-- IdeSwitcherSettings.kt        # 持久化状态
|   |   |-- IdeSwitcherConfigurable.kt    # Settings UI（表格 + 单选按钮）
|   |-- widget/TargetWidget.kt            # 状态栏 Widget
|   |-- startup/WelcomeActivity.kt        # 首次运行通知
|   |-- extension/JumperProvider.kt       # 第三方扩展点
|-- agentic-ide-extension/                # VS Code 扩展
|   |-- src/extension.ts                  # 核心逻辑
|   |-- src/detect.ts                     # 跨平台 IntelliJ 探测
|-- .github/workflows/build.yml           # CI
|-- CONTRIBUTING.md                       # 如何添加新编辑器
```

---

## 扩展 IDEswitcher

第三方插件可以通过 `JumperProvider` 扩展点注册自定义的编辑器跳转器。详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

```kotlin
class MyEditorProvider : JumperProvider {
    override val id = "my-editor"
    override val displayName = "My Editor"
    override fun createJumper(): Jumper = MyEditorJumper
}
```

在你的 `plugin.xml` 中注册：

```xml
<extensions defaultExtensionNs="io.github.melomei.ideswitcher">
    <jumper implementation="com.example.MyEditorProvider"/>
</extensions>
```

---

## 开发

```bash
# 启动沙盒 IDE
./gradlew runIde

# Kotlin 测试（17 个测试）
./gradlew test

# TypeScript 测试（8 个测试）
cd agentic-ide-extension && npm test
```

### 技术栈

- **IDEA 插件**：Kotlin 1.9 + IntelliJ Platform SDK 2024.1
- **编辑器扩展**：TypeScript + VS Code Extension API
- **构建**：Gradle（IDEA）/ npm + vitest（扩展）
- **CI**：GitHub Actions（macOS + Windows + Linux 矩阵）
- **无运行时外部依赖**

---

## 常见问题

**"Editor not found" 通知：**

1. 确认目标编辑器已安装
2. 如果安装在非标准位置，在 Settings -> Tools -> IDEswitcher 中设置自定义路径
3. JetBrains Toolbox 用户：插件会自动探测 Toolbox 管理的安装

**跳转失败并出现 CLI 错误：**

- CLI 未找到：安装 CLI（如 VS Code -> "Install 'code' command in PATH"）
- 权限不足（macOS/Linux）：`chmod +x <CLI路径>`
- 权限不足（Windows）：尝试以管理员身份运行

**扩展端（跳回 IntelliJ）：**

- "CLI not found"（macOS）：IntelliJ -> Tools -> Create Command-line Launcher
- "CLI not found"（Windows）：确保 `idea64.exe` 在 PATH 中
- "CLI not found"（Linux）：`sudo snap install intellij-idea-ultimate --classic` 或将 `bin/` 加入 PATH

---

## 贡献

欢迎提交 Bug 报告和 Pull Request！详见 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何添加新编辑器、报告 Bug 和提交 PR。

## 许可证

[MIT](LICENSE)
