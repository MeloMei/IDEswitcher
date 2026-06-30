import * as fs from "fs";
import * as path from "path";
import { execFileSync } from "child_process";

// Standard IntelliJ IDEA installation paths on macOS
export const IDEA_DIRS = [
    "/Applications/IntelliJ IDEA CE.app",
    "/Applications/IntelliJ IDEA.app",
    "/Applications/IntelliJ IDEA Community Edition.app",
    "/Applications/IntelliJ IDEA Ultimate.app",
];

export type DetectionSource = "cache" | "path" | "applications" | "toolbox";

export interface IntelliJPaths {
    cli: string;
    app: string;
    source: DetectionSource;
}

let cachedCli: string | null = null;
let cachedApp: string | null = null;

/**
 * Attempt to find `idea` CLI on $PATH using `which`.
 * Returns the resolved absolute path, or null if not found.
 */
function findOnPath(binary: string): string | null {
    try {
        const result = execFileSync("which", [binary], {
            timeout: 3000,
            encoding: "utf-8",
            stdio: ["ignore", "pipe", "ignore"],
        }).trim();
        if (result && fs.existsSync(result)) {
            return result;
        }
    } catch {
        // not found
    }
    return null;
}

/**
 * Derive the .app bundle path from a CLI path found on $PATH.
 * e.g. /Applications/IntelliJ IDEA.app/Contents/MacOS/idea → /Applications/IntelliJ IDEA.app
 */
function deriveAppPathFromCli(cliPath: string): string {
    // Walk up from bin/Contents/MacOS/idea → .app root
    // Typical: <app>/Contents/MacOS/idea → 3 levels up
    const appPath = path.resolve(cliPath, "..", "..", "..");
    return appPath;
}

export function detectIntelliJ(dirs: string[] = IDEA_DIRS): IntelliJPaths | null {
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

    // Strategy 1: $PATH lookup (most reliable — works for Toolbox, Homebrew, etc.)
    const pathCli = findOnPath("idea");
    if (pathCli) {
        const appPath = deriveAppPathFromCli(pathCli);
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
        } catch {
            continue;
        }
    }

    // Strategy 3: JetBrains Toolbox managed installations
    const home = process.env.HOME || "";
    if (home) {
        const toolboxBase = `${home}/Library/Application Support/JetBrains/Toolbox/apps`;
        const toolboxPatterns = [
            "IDEA-U",
            "IDEA-C",
        ];
        for (const product of toolboxPatterns) {
            const productDir = `${toolboxBase}/${product}`;
            try {
                const versions = fs.readdirSync(productDir).filter(
                    (v) => !v.startsWith(".")
                ).sort().reverse();
                for (const ver of versions) {
                    const candidate = `${productDir}/${ver}/IntelliJ IDEA.app/Contents/MacOS/idea`;
                    try {
                        fs.accessSync(candidate, fs.constants.X_OK);
                        const appDir = path.resolve(candidate, "..", "..", "..");
                        cachedCli = candidate;
                        cachedApp = appDir;
                        return { cli: candidate, app: appDir, source: "toolbox" };
                    } catch {
                        continue;
                    }
                }
            } catch {
                continue;
            }
        }
    }

    return null;
}

export function clearCache(): void {
    cachedCli = null;
    cachedApp = null;
}
