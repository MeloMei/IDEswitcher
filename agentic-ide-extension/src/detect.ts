import * as fs from "fs";
import * as path from "path";
import { execFileSync } from "child_process";

// macOS: Standard IntelliJ IDEA installation paths
export const IDEA_DIRS_MAC = [
    "/Applications/IntelliJ IDEA CE.app",
    "/Applications/IntelliJ IDEA.app",
    "/Applications/IntelliJ IDEA Community Edition.app",
    "/Applications/IntelliJ IDEA Ultimate.app",
];

// Windows: Standard IntelliJ IDEA installation paths
export const IDEA_DIRS_WIN = [
    `${process.env.PROGRAMFILES || "C:\\Program Files"}\\JetBrains`,
    `${process.env["PROGRAMFILES(X86)"] || "C:\\Program Files (x86)"}\\JetBrains`,
];

export type DetectionSource = "cache" | "path" | "applications" | "toolbox";

export interface IntelliJPaths {
    cli: string;
    app: string;
    source: DetectionSource;
}

let cachedCli: string | null = null;
let cachedApp: string | null = null;

function findOnPathMac(binary: string): string | null {
    try {
        const result = execFileSync("which", [binary], {
            timeout: 3000,
            encoding: "utf-8",
            stdio: ["ignore", "pipe", "ignore"],
        }).trim();
        if (result && fs.existsSync(result)) return result;
    } catch { /* not found */ }
    return null;
}

function findOnPathWin(binary: string): string | null {
    try {
        const result = execFileSync("where", [binary], {
            timeout: 3000,
            encoding: "utf-8",
            stdio: ["ignore", "pipe", "ignore"],
        }).trim().split("\n")[0].trim();
        if (result && fs.existsSync(result)) return result;
    } catch { /* not found */ }
    return null;
}

function findOnPath(binary: string, win: boolean): string | null {
    return win ? findOnPathWin(binary) : findOnPathMac(binary);
}

function deriveAppPathFromCliMac(cliPath: string): string {
    return path.resolve(cliPath, "..", "..", "..");
}

function deriveAppPathFromCliWin(cliPath: string): string {
    // e.g. C:\Program Files\JetBrains\IntelliJ IDEA 2024.1\bin\idea64.exe
    //   → C:\Program Files\JetBrains\IntelliJ IDEA 2024.1
    return path.resolve(cliPath, "..", "..");
}

function detectMac(dirs: string[]): IntelliJPaths | null {
    // Strategy 1: $PATH lookup
    const pathCli = findOnPath("idea", false);
    if (pathCli) {
        const appPath = deriveAppPathFromCliMac(pathCli);
        cachedCli = pathCli;
        cachedApp = appPath;
        return { cli: pathCli, app: appPath, source: "path" };
    }

    // Strategy 2: standard /Applications/ paths
    for (const dir of dirs) {
        const candidate = `${dir}/Contents/MacOS/idea`;
        try {
            fs.accessSync(candidate, fs.constants.X_OK);
            cachedCli = candidate;
            cachedApp = dir;
            return { cli: candidate, app: dir, source: "applications" };
        } catch { continue; }
    }

    // Strategy 3: Toolbox
    const home = process.env.HOME || "";
    if (home) {
        const toolboxBase = `${home}/Library/Application Support/JetBrains/Toolbox/apps`;
        for (const product of ["IDEA-U", "IDEA-C"]) {
            const productDir = `${toolboxBase}/${product}`;
            try {
                const versions = fs.readdirSync(productDir)
                    .filter((v) => !v.startsWith(".")).sort().reverse();
                for (const ver of versions) {
                    const candidate = `${productDir}/${ver}/IntelliJ IDEA.app/Contents/MacOS/idea`;
                    try {
                        fs.accessSync(candidate, fs.constants.X_OK);
                        const appDir = path.resolve(candidate, "..", "..", "..");
                        cachedCli = candidate;
                        cachedApp = appDir;
                        return { cli: candidate, app: appDir, source: "toolbox" };
                    } catch { continue; }
                }
            } catch { continue; }
        }
    }

    return null;
}

function detectWin(): IntelliJPaths | null {
    // Strategy 1: %PATH% lookup
    const pathCli = findOnPath("idea64.exe", true) || findOnPath("idea.exe", true);
    if (pathCli) {
        const appPath = deriveAppPathFromCliWin(pathCli);
        cachedCli = pathCli;
        cachedApp = appPath;
        return { cli: pathCli, app: appPath, source: "path" };
    }

    // Strategy 2: standard Program Files paths
    for (const jetbrainsDir of IDEA_DIRS_WIN) {
        if (!fs.existsSync(jetbrainsDir)) continue;
        try {
            const entries = fs.readdirSync(jetbrainsDir)
                .filter((e) => e.startsWith("IntelliJ IDEA"))
                .sort().reverse();
            for (const entry of entries) {
                const ideaDir = path.join(jetbrainsDir, entry);
                for (const bin of ["idea64.exe", "idea.exe"]) {
                    const candidate = path.join(ideaDir, "bin", bin);
                    if (fs.existsSync(candidate)) {
                        cachedCli = candidate;
                        cachedApp = ideaDir;
                        return { cli: candidate, app: ideaDir, source: "applications" };
                    }
                }
            }
        } catch { continue; }
    }

    // Strategy 3: Toolbox on Windows
    const localAppData = process.env.LOCALAPPDATA || "";
    if (localAppData) {
        const toolboxBase = path.join(localAppData, "JetBrains", "Toolbox", "apps");
        for (const product of ["IDEA-U", "IDEA-C"]) {
            const productDir = path.join(toolboxBase, product);
            if (!fs.existsSync(productDir)) continue;
            try {
                const versions = fs.readdirSync(productDir)
                    .filter((v) => !v.startsWith(".")).sort().reverse();
                for (const ver of versions) {
                    const ideaDir = path.join(productDir, ver, "IntelliJ IDEA");
                    for (const bin of ["idea64.exe", "idea.exe"]) {
                        const candidate = path.join(ideaDir, "bin", bin);
                        if (fs.existsSync(candidate)) {
                            cachedCli = candidate;
                            cachedApp = ideaDir;
                            return { cli: candidate, app: ideaDir, source: "toolbox" };
                        }
                    }
                }
            } catch { continue; }
        }
    }

    return null;
}

export function detectIntelliJ(
    dirs: string[] = IDEA_DIRS_MAC,
    platform: NodeJS.Platform = process.platform,
): IntelliJPaths | null {
    const win = platform === "win32";

    // Check cache with validation
    if (cachedCli && cachedApp) {
        try {
            fs.accessSync(cachedCli, fs.constants.X_OK);
            return { cli: cachedCli, app: cachedApp, source: "cache" };
        } catch {
            cachedCli = null;
            cachedApp = null;
        }
    }

    return win ? detectWin() : detectMac(dirs);
}

export function clearCache(): void {
    cachedCli = null;
    cachedApp = null;
}
