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
                    'Please install IntelliJ IDEA in /Applications/ or run it at least once.'
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
                    const message =
                        (error as any).code === 'ENOENT'
                            ? 'IntelliJ IDEA not found.\n\nPlease make sure IntelliJ IDEA is installed.'
                            : `Failed to jump to IntelliJ IDEA: ${error.message}`;
                    vscode.window.showErrorMessage(message);
                }
            });
        }
    );

    context.subscriptions.push(disposable);
}

export function deactivate() {}