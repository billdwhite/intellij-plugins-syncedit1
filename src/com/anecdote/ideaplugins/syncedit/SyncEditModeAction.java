package com.anecdote.ideaplugins.syncedit;

import com.anecdote.ideaplugins.util.EditorUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class SyncEditModeAction
extends AnAction
implements IntentionAction
{

    public SyncEditModeAction()
    {
    }



    public void update(final AnActionEvent e)
    {
        super.update(e);
        boolean enable = true;
        Editor editor = EditorUtils.getEditor(e);
        if (!SyncEditModeController.isInSyncEditMode(editor))
        {
            SyncEditModeController.leaveSyncEditMode();
            if (editor == null || !editor.getSelectionModel().hasSelection())
            {
                enable = false;
            }
        }
        e.getPresentation().setEnabled(enable);
    }



    public void actionPerformed(AnActionEvent e)
    {
        Editor editor = EditorUtils.getEditor(e);
        executeForEditor(editor);
    }



    public static void executeForEditor(Editor editor)
    {
        if (SyncEditModeController.isInSyncEditMode(editor))
        {
            if (editor.getCaretModel().getOffset() < SyncEditModeController.getActiveRangeBoxHighlighter().getStartOffset() ||
            editor.getCaretModel().getOffset() > SyncEditModeController.getActiveRangeBoxHighlighter().getEndOffset())
            {
                if (editor.getSelectionModel().hasSelection())
                {

                    SyncEditModeController.leaveSyncEditMode();
                    SyncEditModeController.enterSyncEditMode(editor);
                }
            }
            else
            {
                SyncEditModeController.activateSyncEditSelectionForCaretLocation(editor);
            }
        }
        else
        {
            SyncEditModeController.enterSyncEditMode(editor);
        }
    }



    public String getText()
    {
        return "Enter SyncEdit Mode for selection";
    }



    public String getFamilyName()
    {
        return "SyncEdit";
    }



    public boolean isAvailable(Project project, Editor editor, PsiFile file)
    {
        return editor.getSelectionModel().hasSelection() && !SyncEditModeController.isInSyncEditMode();
    }



    public void invoke(Project project, Editor editor, PsiFile file)
    throws IncorrectOperationException
    {
        executeForEditor(editor);
    }



    public boolean startInWriteAction()
    {
        return false;
    }
}
