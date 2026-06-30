import * as vscode from 'vscode';
import { execFile } from 'child_process';
import { detectIntelliJ } from './detect';

export function activate(context: vscode.ExtensionContext) {
    const disposable = vscode.commands.registerCommand(
        'ideSwitcher.jumpToIntelliJ',
        () => {
            const idea = detectIntelliJ();
            if (!idea) {
                vscode.window.showErrorMessage(
                    'IntelliJ IDEA not found.\n\n' +
                    'Tried: $PATH lookup, /Applications/, JetBrains Toolbox.\n\n' +
                    'Make sure IntelliJ IDEA is installed and the `idea` CLI is available.\n' +
                    'In IDEA: Tools → Create Command-line Launcher.'
                );
                return;
            }

            const editor = vscode.window.activeTextEditor;

            if (!editor || editor.document.uri.scheme !== 'file') {
                execFile('open', [idea.app], { timeout: 10000 }, (error) => {
                    if (error) {
                        vscode.window.showErrorMessage(
                            `Failed to open IntelliJ IDEA: ${error.message}`
                        );
                    }
                });
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
                        message =
                            'IntelliJ IDEA CLI not found.\n\n' +
                            'Open IntelliJ → Tools → Create Command-line Launcher to install the `idea` CLI.';
                    } else if (code === 'ETIMEDOUT' || (error as any).killed) {
                        message =
                            'IntelliJ IDEA took too long to respond (>10s).\n\n' +
                            'It may be busy indexing. Try again in a moment.';
                    } else if ((error as any).code === 'EACCES') {
                        message =
                            `Permission denied: ${idea.cli}\n\n` +
                            'Run: chmod +x "' + idea.cli + '"';
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