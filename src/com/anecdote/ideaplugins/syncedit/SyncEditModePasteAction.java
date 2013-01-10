package com.anecdote.ideaplugins.syncedit;

import com.anecdote.ideaplugins.util.EditorUtils;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;

public class SyncEditModePasteAction
extends AnAction
{

    public void update(AnActionEvent e)
    {
        AnAction pasteAction = ActionManager.getInstance().getAction("$Paste");
        pasteAction.update(new AnActionEvent(e.getInputEvent(),
                                             e.getDataContext(),
                                             e.getPlace(),
                                             e.getPresentation(),
                                             e.getActionManager(),
                                             e.getModifiers()));
    }



    public void actionPerformed(AnActionEvent e)
    {
        SyncEditModeController.leaveSyncEditMode();
        AnAction pasteAction = ActionManager.getInstance().getAction("$Paste");
        Document document = EditorUtils.getEditor(e).getDocument();
        final int[] pasteOffset = new int[]{0};
        final int[] pasteLength = new int[]{0};
        DocumentAdapter documentAdapter = new DocumentAdapter()
        {
            public void documentChanged(DocumentEvent e)
            {
                pasteOffset[0] = e.getOffset();
                pasteLength[0] = e.getNewLength();
            }
        };
        document.addDocumentListener(documentAdapter);
        try
        {
            pasteAction.actionPerformed(e);
            SyncEditModeController.enterSyncEditMode(EditorUtils.getEditor(e), pasteOffset[0],
                                                     pasteOffset[0] + pasteLength[0]);
        }
        finally
        {
            document.removeDocumentListener(documentAdapter);
        }
    }
}
