import * as fs from "fs";

// Common IntelliJ IDEA installation paths on macOS
export const IDEA_DIRS = [
    "/Applications/IntelliJ IDEA CE.app",
    "/Applications/IntelliJ IDEA.app",
    "/Applications/IntelliJ IDEA Community Edition.app",
    "/Applications/IntelliJ IDEA Ultimate.app",
];

export interface IntelliJPaths {
    cli: string;
    app: string;
}

let cachedCli: string | null = null;
let cachedApp: string | null = null;

export function detectIntelliJ(dirs: string[] = IDEA_DIRS): IntelliJPaths | null {
    if (cachedCli && cachedApp) {
        // Validate cache is still valid
        try {
            fs.accessSync(cachedCli, fs.constants.X_OK);
            return { cli: cachedCli, app: cachedApp };
        } catch {
            cachedCli = null;
            cachedApp = null;
        }
    }

    for (const dir of dirs) {
        const candidate = `${dir}/Contents/MacOS/idea`;
        try {
            fs.accessSync(candidate, fs.constants.X_OK);
            cachedCli = candidate;
            cachedApp = dir;
            return { cli: candidate, app: dir };
        } catch {
            continue;
        }
    }
    return null;
}

export function clearCache(): void {
    cachedCli = null;
    cachedApp = null;
}
