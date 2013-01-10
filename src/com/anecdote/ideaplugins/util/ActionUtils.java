package com.anecdote.ideaplugins.util;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.keymap.KeymapManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionUtils
{

    private static Map<String, EditorActionHandler> _originalActionHandlers = new HashMap<String, EditorActionHandler>();
    private static Map<String, AnAction> _originalActions = new HashMap<String, AnAction>();
    private static Map<KeyStroke, List<String>> _disabledActionsForKeystrokes = new HashMap<KeyStroke, List<String>>();
    private static final EditorActionHandler DISABLED_ACTION_HANDLER = new EditorActionHandler()
    {
        public boolean isEnabled(Editor editor, DataContext dataContext)
        {
            return false;
        }


        public void execute(Editor editor, DataContext dataContext)
        {
        }
    };



    private ActionUtils()
    {
    }



    public static boolean installActionHandlerOverride(String actionID, EditorActionHandler editorActionHandler)
    {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction anAction = actionManager.getAction(actionID);
        if (anAction instanceof EditorAction)
        {
            EditorAction editorAction = (EditorAction) anAction;
            EditorActionHandler oldActionHandler = editorAction.getHandler();
            _originalActionHandlers.put(actionID, oldActionHandler);
            editorAction.setupHandler(editorActionHandler);
            return true;
        }
        return false;
    }



    public static void installActionOverride(String actionID, AnAction action)
    {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction oldAction = actionManager.getAction(actionID);
        _originalActions.put(actionID, oldAction);
        actionManager.unregisterAction(actionID);
        actionManager.registerAction(actionID, action);
    }



    public static EditorActionHandler getOriginalActionHandler(String actionID)
    {
        return _originalActionHandlers.get(actionID);
    }



    public static AnAction getOriginalAction(String actionID)
    {
        return _originalActions.get(actionID);
    }



    public static void handleActionWithOriginalHandler(String actionID, Editor editor, DataContext dataContext)
    {
        EditorActionHandler actionHandler = getOriginalActionHandler(actionID);
        if (actionHandler != null)
        {
            actionHandler.execute(editor, dataContext);
        }
    }



    public static boolean restoreOriginalActionHandler(String actionID)
    {
        EditorActionManager editorActionManager = EditorActionManager.getInstance();
        EditorActionHandler actionHandler = _originalActionHandlers.remove(actionID);
        if (actionHandler != null)
        {
            editorActionManager.setActionHandler(actionID, actionHandler);
            return true;
        }
        return false;
    }



    public static void restoreOriginalAction(String actionID)
    {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction originalAction = _originalActions.remove(actionID);
        actionManager.unregisterAction(actionID);
        if (originalAction != null)
        {
            actionManager.registerAction(actionID, originalAction);
        }
    }



    public static void disableOtherActionsOnSameKeystrokes(String actionID)
    {
        KeymapManager keymapManager = KeymapManager.getInstance();
        Shortcut[] shortcuts = keymapManager.getActiveKeymap().getShortcuts(actionID);
        for (int i = 0; i < shortcuts.length; i++)
        {
            Shortcut shortcut = shortcuts[i];
            if (shortcut instanceof KeyboardShortcut)
            {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut) shortcut;
                disableActionsForKeystroke(keyboardShortcut.getFirstKeyStroke(), actionID);
            }
        }
    }



    public static void enableOtherActionsOnSameKeystrokes(String actionID)
    {
        KeymapManager keymapManager = KeymapManager.getInstance();
        Shortcut[] shortcuts = keymapManager.getActiveKeymap().getShortcuts(actionID);
        for (int i = 0; i < shortcuts.length; i++)
        {
            Shortcut shortcut = shortcuts[i];
            if (shortcut instanceof KeyboardShortcut)
            {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut) shortcut;
                enableActionsForKeystroke(keyboardShortcut.getFirstKeyStroke());
            }
        }
    }



    public static void disableActionsForKeystroke(KeyStroke keyStroke, String excludeActionID)
    {
        KeymapManager keymapManager = KeymapManager.getInstance();
        String[] actionIDs = keymapManager.getActiveKeymap().getActionIds(keyStroke);
        List<String> actionsDisabled = new ArrayList<String>(actionIDs.length);
        for (int i = 0; i < actionIDs.length; i++)
        {
            String actionID = actionIDs[i];
            if (!actionID.equals(excludeActionID) && !isRetainedAction(actionID))
            {
                if (!installActionHandlerOverride(actionID, DISABLED_ACTION_HANDLER))
                {
                    installActionOverride(actionID, new EditorAction(DISABLED_ACTION_HANDLER)
                    {
                    });
                }
                actionsDisabled.add(actionID);
            }
        }
        _disabledActionsForKeystrokes.put(keyStroke, actionsDisabled);
    }



    private static boolean isRetainedAction(String actionID)
    {
        return actionID.startsWith("EditorChooseLookupItem");
    }



    public static void enableActionsForKeystroke(KeyStroke keyStroke)
    {
        List<String> actionIDs = _disabledActionsForKeystrokes.get(keyStroke);
        if (actionIDs != null)
        {
            for (int i = 0; i < actionIDs.size(); i++)
            {
                String actionID = actionIDs.get(i);
                if (!restoreOriginalActionHandler(actionID))
                {
                    restoreOriginalAction(actionID);
                }
            }
        }
    }
}
