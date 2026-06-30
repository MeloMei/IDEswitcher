import * as vscode from 'vscode';
import { execFile } from 'child_process';
import { detectIntelliJ } from './detect';

const isWindows = process.platform === 'win32';

export function activate(context: vscode.ExtensionContext) {
    const disposable = vscode.commands.registerCommand(
        'ideSwitcher.jumpToIntelliJ',
        () => {
            const idea = detectIntelliJ();
            if (!idea) {
                const triedMsg = isWindows
                    ? 'Tried: %PATH% lookup, Program Files, JetBrains Toolbox.'
                    : 'Tried: $PATH lookup, /Applications/, JetBrains Toolbox.';
                const hintMsg = isWindows
                    ? 'Make sure IntelliJ IDEA is installed. You can also add idea64.exe to your PATH.'
                    : 'Make sure IntelliJ IDEA is installed and the `idea` CLI is available.\nIn IDEA: Tools → Create Command-line Launcher.';
                vscode.window.showErrorMessage(
                    `IntelliJ IDEA not found.\n\n${triedMsg}\n\n${hintMsg}`
                );
                return;
            }

            const editor = vscode.window.activeTextEditor;

            if (!editor || editor.document.uri.scheme !== 'file') {
                // Open IntelliJ without a specific file
                if (isWindows) {
                    execFile('cmd', ['/c', 'start', '', idea.app], { timeout: 10000 }, (error) => {
                        if (error) {
                            vscode.window.showErrorMessage(
                                `Failed to open IntelliJ IDEA: ${error.message}`
                            );
                        }
                    });
                } else {
                    execFile('open', [idea.app], { timeout: 10000 }, (error) => {
                        if (error) {
                            vscode.window.showErrorMessage(
                                `Failed to open IntelliJ IDEA: ${error.message}`
                            );
                        }
                    });
                }
                return;
            }

            const filePath = editor.document.uri.fsPath;
            const line = String(editor.selection.active.line + 1);
            const col = String(editor.selection.active.character + 1);
            const args = ['--line', line, '--column', col, filePath];

            execFile(idea.cli, args, { timeout: 10000 }, (error) => {
                if (error) {
                    let message: string;
                    const code = (error as any).code;
                    if (code === 'ENOENT') {
                        message = isWindows
                            ? 'IntelliJ IDEA CLI not found (idea64.exe).\n\nMake sure IntelliJ is installed and its bin directory is on your PATH.'
                            : 'IntelliJ IDEA CLI not found.\n\nOpen IntelliJ → Tools → Create Command-line Launcher to install the `idea` CLI.';
                    } else if (code === 'ETIMEDOUT' || (error as any).killed) {
                        message =
                            'IntelliJ IDEA took too long to respond (>10s).\n\n' +
                            'It may be busy indexing. Try again in a moment.';
                    } else if (code === 'EACCES') {
                        message = isWindows
                            ? `Access denied: ${idea.cli}\n\nTry running your editor as Administrator.`
                            : `Permission denied: ${idea.cli}\n\nRun: chmod +x "${idea.cli}"`;
                    } else {
                        message = `Failed to jump to IntelliJ IDEA: ${error.message}`;
                    }
                    vscode.window.showErrorMessage(message);
                }
            });
        }
    );

    context.subscriptions.push(disposable);
}

export function deactivate() {}