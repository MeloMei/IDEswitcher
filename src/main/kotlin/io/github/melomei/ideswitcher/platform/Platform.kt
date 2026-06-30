package io.github.melomei.ideswitcher.platform

import com.intellij.openapi.util.SystemInfo

enum class Platform {
    MACOS, WINDOWS, LINUX;

    companion object {
        val current: Platform by lazy {
            when {
                SystemInfo.isMac -> MACOS
                SystemInfo.isWindows -> WINDOWS
                SystemInfo.isLinux -> LINUX
                else -> MACOS // fallback
            }
        }
    }
}
