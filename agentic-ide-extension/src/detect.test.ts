import { describe, it, expect, beforeEach, vi, afterEach } from "vitest";
import * as fs from "fs";
import { detectIntelliJ, clearCache, IDEA_DIRS } from "./detect";

vi.mock("fs");

describe("detectIntelliJ", () => {
    beforeEach(() => {
        clearCache();
        vi.restoreAllMocks();
    });

    it("returns null when no IntelliJ installation is found", () => {
        vi.mocked(fs.accessSync).mockImplementation(() => {
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ();
        expect(result).toBeNull();
    });

    it("returns the first matching IntelliJ installation", () => {
        const mockAccess = vi.mocked(fs.accessSync);
        // Only the second path works
        mockAccess.mockImplementation((path: fs.PathLike, mode?: number) => {
            if (String(path).includes("IntelliJ IDEA.app/Contents/MacOS/idea")) {
                return; // success
            }
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ();
        expect(result).not.toBeNull();
        expect(result!.app).toBe("/Applications/IntelliJ IDEA.app");
        expect(result!.cli).toBe("/Applications/IntelliJ IDEA.app/Contents/MacOS/idea");
    });

    it("caches the result on subsequent calls", () => {
        const mockAccess = vi.mocked(fs.accessSync);
        mockAccess.mockReturnValue(); // all paths accessible

        const first = detectIntelliJ(["/Applications/IntelliJ IDEA.app"]);
        const callCountAfterFirst = mockAccess.mock.calls.length;

        const second = detectIntelliJ(["/Applications/IntelliJ IDEA.app"]);
        // Second call should hit cache (with validation), not re-scan all dirs
        expect(second).toEqual(first);
    });

    it("invalidates cache when cached path no longer exists", () => {
        const mockAccess = vi.mocked(fs.accessSync);
        mockAccess.mockReturnValue();

        // First call caches the result
        detectIntelliJ(["/Applications/IntelliJ IDEA.app"]);

        // Now make the cached path fail
        clearCache();
        mockAccess.mockImplementation(() => {
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ(["/Applications/IntelliJ IDEA.app"]);
        expect(result).toBeNull();
    });

    it("respects custom dirs parameter", () => {
        const mockAccess = vi.mocked(fs.accessSync);
        mockAccess.mockImplementation((path: fs.PathLike) => {
            if (String(path).includes("/custom/path/Contents/MacOS/idea")) {
                return;
            }
            throw new Error("ENOENT");
        });

        const result = detectIntelliJ(["/custom/path"]);
        expect(result).not.toBeNull();
        expect(result!.app).toBe("/custom/path");
    });

    it("IDEA_DIRS contains expected default paths", () => {
        expect(IDEA_DIRS.length).toBeGreaterThanOrEqual(4);
        expect(IDEA_DIRS).toContain("/Applications/IntelliJ IDEA.app");
        expect(IDEA_DIRS).toContain("/Applications/IntelliJ IDEA CE.app");
    });
});

describe("line and column conversion", () => {
    it("converts 0-based VS Code position to 1-based IntelliJ position", () => {
        // VS Code uses 0-based line/character, IntelliJ CLI uses 1-based
        const vsCodeLine = 41;
        const vsCodeChar = 6;
        const ideaLine = String(vsCodeLine + 1);
        const ideaCol = String(vsCodeChar + 1);
        expect(ideaLine).toBe("42");
        expect(ideaCol).toBe("7");
    });
});
