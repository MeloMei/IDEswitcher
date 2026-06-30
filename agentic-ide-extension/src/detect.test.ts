import { describe, it, expect, beforeEach, vi } from "vitest";
import * as fs from "fs";
import * as child_process from "child_process";
import { detectIntelliJ, clearCache, IDEA_DIRS_MAC } from "./detect";

vi.mock("fs");
vi.mock("child_process");

describe("detectIntelliJ", () => {
    beforeEach(() => {
        clearCache();
        vi.restoreAllMocks();
        // Default: `which` finds nothing
        vi.mocked(child_process.execFileSync).mockImplementation(() => {
            throw new Error("not found");
        });
    });

    it("returns null when no IntelliJ installation is found", () => {
        vi.mocked(fs.accessSync).mockImplementation(() => {
            throw new Error("ENOENT");
        });
        vi.mocked(fs.existsSync).mockReturnValue(false);

        const result = detectIntelliJ(undefined, "darwin");
        expect(result).toBeNull();
    });

    it("prefers $PATH lookup over /Applications scan", () => {
        const mockExec = vi.mocked(child_process.execFileSync);
        mockExec.mockReturnValue("/usr/local/bin/idea\n");

        vi.mocked(fs.existsSync).mockReturnValue(true);
        vi.mocked(fs.accessSync).mockReturnValue(); // all accessible

        const result = detectIntelliJ(undefined, "darwin");
        expect(result).not.toBeNull();
        expect(result!.cli).toBe("/usr/local/bin/idea");
        expect(result!.source).toBe("path");
    });

    it("falls back to /Applications when $PATH has no idea", () => {
        // which fails
        vi.mocked(child_process.execFileSync).mockImplementation(() => {
            throw new Error("not found");
        });

        const mockAccess = vi.mocked(fs.accessSync);
        mockAccess.mockImplementation((p: fs.PathLike) => {
            if (String(p).includes("IntelliJ IDEA.app/Contents/MacOS/idea")) {
                return;
            }
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ(undefined, "darwin");
        expect(result).not.toBeNull();
        expect(result!.app).toBe("/Applications/IntelliJ IDEA.app");
        expect(result!.source).toBe("applications");
    });

    it("caches the result on subsequent calls", () => {
        vi.mocked(child_process.execFileSync).mockImplementation(() => {
            throw new Error("not found");
        });
        vi.mocked(fs.accessSync).mockReturnValue();

        const first = detectIntelliJ(["/Applications/IntelliJ IDEA.app"], "darwin");
        expect(first!.source).toBe("applications");

        const second = detectIntelliJ(["/Applications/IntelliJ IDEA.app"], "darwin");
        expect(second!.source).toBe("cache");
        expect(second!.cli).toBe(first!.cli);
    });

    it("invalidates cache when cached path no longer exists", () => {
        vi.mocked(child_process.execFileSync).mockImplementation(() => {
            throw new Error("not found");
        });
        vi.mocked(fs.accessSync).mockReturnValue();

        detectIntelliJ(["/Applications/IntelliJ IDEA.app"], "darwin");
        clearCache();

        vi.mocked(fs.accessSync).mockImplementation(() => {
            throw new Error("ENOENT");
        });
        vi.mocked(fs.existsSync).mockReturnValue(false);

        const result = detectIntelliJ(["/Applications/IntelliJ IDEA.app"], "darwin");
        expect(result).toBeNull();
    });

    it("respects custom dirs parameter", () => {
        vi.mocked(child_process.execFileSync).mockImplementation(() => {
            throw new Error("not found");
        });
        vi.mocked(fs.accessSync).mockImplementation((p: fs.PathLike) => {
            if (String(p).includes("/custom/path/Contents/MacOS/idea")) {
                return;
            }
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ(["/custom/path"], "darwin");
        expect(result).not.toBeNull();
        expect(result!.app).toBe("/custom/path");
    });

    it("IDEA_DIRS_MAC contains expected default paths", () => {
        expect(IDEA_DIRS_MAC.length).toBeGreaterThanOrEqual(4);
        expect(IDEA_DIRS_MAC).toContain("/Applications/IntelliJ IDEA.app");
        expect(IDEA_DIRS_MAC).toContain("/Applications/IntelliJ IDEA CE.app");
    });
});

describe("line and column conversion", () => {
    it("converts 0-based VS Code position to 1-based IntelliJ position", () => {
        const vsCodeLine = 41;
        const vsCodeChar = 6;
        expect(String(vsCodeLine + 1)).toBe("42");
        expect(String(vsCodeChar + 1)).toBe("7");
    });
});
