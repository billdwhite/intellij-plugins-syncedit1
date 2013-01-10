package com.anecdote.ideaplugins.syncedit;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

public class NextSyncEditableWordAction
extends SelectSyncEditableWordAction
{

    public NextSyncEditableWordAction()
    {
        super(new EditorActionHandler()
        {
            public void execute(Editor editor, DataContext dataContext)
            {
                if (SyncEditModeController.isInSyncEditMode(editor))
                {
                    SyncEditModeController.selectNextRepeatedWord();
                }
            }
        });
    }
}
