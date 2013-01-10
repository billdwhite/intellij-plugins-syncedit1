package com.anecdote.ideaplugins.syncedit;

import com.anecdote.ideaplugins.util.ActionUtils;
import com.anecdote.ideaplugins.util.EditorUtils;
import com.anecdote.ideaplugins.util.Word;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.util.TextRange;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings({"NonFinalStaticVariableUsedInClassInitialization", "StringConcatenation"})
public class SyncEditModeController
{

    private static final String ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION =
    IdeActions.ACTION_EDITOR_MOVE_LINE_START + "WithSelection";
    private static final String ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION =
    IdeActions.ACTION_EDITOR_MOVE_LINE_END + "WithSelection";

    private static boolean _hasSyncEditSelection;
    private static RangeHighlighter _selectedWordBoxHighlight;
    private static RangeHighlighter _selectedWordColorHighlight;
    private static List _matchingWordColorHighlighters = new ArrayList();
    private static RangeHighlighter _activeRangeBoxHighlighter;
    private static Editor _activeEditor;
    private static boolean _modifyingDocument;

    private static DocumentAdapter _documentListener = new DocumentAdapter()
    {
        public void beforeDocumentChange(DocumentEvent e)
        {
            beforeActiveEditorDocumentChange(e);
        }



        public void documentChanged(final DocumentEvent e)
        {
            activeEditorDocumentChange(e);
        }
    };

    private static EditorActionHandler _escapeHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor != null)
            {
                if (_activeEditor == editor)
                {
                    doEscape();
                }
                else
                { // executed on a different editor - leave mode
                    ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_ESCAPE, editor, dataContext);
                    leaveSyncEditMode();
                }
            }
        }
    };

    private static EditorActionHandler _enterHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor != null)
            {
                if (_activeEditor == editor)
                {
                    doEscape();
                }
                else
                { // executed on a different editor - leave mode
                    ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_ENTER, editor, dataContext);
                    leaveSyncEditMode();
                }
            }
        }
    };

    private static EditorActionHandler _homeHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor == editor)
            {
                if (editor.getCaretModel().getOffset() > _selectedWordBoxHighlight.getStartOffset() &&
                editor.getCaretModel().getOffset() <= _selectedWordBoxHighlight.getEndOffset())
                {
                    EditorUtils.moveCaretBackTo(editor, _selectedWordBoxHighlight.getStartOffset(), false);
                }
                else
                {
                    ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_START,
                                                                editor,
                                                                dataContext);
                }
            }
            else
            { // executed on a different editor - leave mode
                ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_START,
                                                            editor,
                                                            dataContext);
                leaveSyncEditMode();
            }
        }
    };
    private static EditorActionHandler _homeWithSelectionHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor == editor)
            {
                if (editor.getCaretModel().getOffset() > _selectedWordBoxHighlight.getStartOffset() &&
                editor.getCaretModel().getOffset() <= _selectedWordBoxHighlight.getEndOffset())
                {
                    EditorUtils.moveCaretBackTo(editor, _selectedWordBoxHighlight.getStartOffset(), true);
                }
                else
                {
                    ActionUtils.handleActionWithOriginalHandler(ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION,
                                                                editor,
                                                                dataContext);
                }
            }
            else
            { // executed on a different editor - leave mode
                ActionUtils.handleActionWithOriginalHandler(ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION,
                                                            editor,
                                                            dataContext);
                leaveSyncEditMode();
            }
        }
    };

    private static EditorActionHandler _endHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor == editor)
            {
                if (editor.getCaretModel().getOffset() < _selectedWordBoxHighlight.getEndOffset() &&
                editor.getCaretModel().getOffset() >= _selectedWordBoxHighlight.getStartOffset())
                {
                    EditorUtils.moveCaretForwardTo(editor, _selectedWordBoxHighlight.getEndOffset(), false);
                }
                else
                {
                    ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END,
                                                                editor,
                                                                dataContext);
                }
            }
            else
            { // executed on a different editor - leave mode
                ActionUtils.handleActionWithOriginalHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END,
                                                            editor,
                                                            dataContext);
                leaveSyncEditMode();
            }
        }
    };
    private static EditorActionHandler _endWithSelectionHandler = new EditorActionHandler()
    {
        public void execute(Editor editor, DataContext dataContext)
        {
            if (_activeEditor == editor)
            {
                if (editor.getCaretModel().getOffset() < _selectedWordBoxHighlight.getEndOffset() &&
                editor.getCaretModel().getOffset() >= _selectedWordBoxHighlight.getStartOffset())
                {
                    EditorUtils.moveCaretForwardTo(editor, _selectedWordBoxHighlight.getEndOffset(), true);
                }
                else
                {
                    ActionUtils.handleActionWithOriginalHandler(ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION,
                                                                editor,
                                                                dataContext);
                }
            }
            else
            { // executed on a different editor - leave mode
                ActionUtils.handleActionWithOriginalHandler(ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION,
                                                            editor,
                                                            dataContext);
                leaveSyncEditMode();
            }
        }
    };

    private static Map<String, Word> _wordMap = new HashMap<String, Word>();
    private static List<Word> _words;
    private static List<Word> _repeatedWords;
    private static int _selectedRepeatedWordIndex = -1;



    private SyncEditModeController()
    {
    }



    static void enterSyncEditMode(Editor editor)
    {
        if (editor != null)
        {
            SelectionModel selectionModel = editor.getSelectionModel();
            if (selectionModel.hasSelection())
            {
                int start = selectionModel.getSelectionStart();
                int end = selectionModel.getSelectionEnd();
                if (start != end)
                {
                    if (start > end)
                    {
                        int temp = end;
                        end = start;
                        start = temp;
                    }
                    enterSyncEditMode(editor, start, end);
                    selectionModel.removeSelection();
                }
            }
        }
    }



    public static void enterSyncEditMode(Editor editor, int rangeStart, int rangeEnd)
    {
        if (_activeEditor != null && _activeEditor != editor)
        {
            leaveSyncEditMode();
        }
        _activeEditor = editor;
        try
        {
            Color effectColor = EditorColors.SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes().getBackgroundColor();
            if (effectColor == null)
            {
                effectColor = Color.magenta;
            }
            TextAttributes rangeAttributes =
            editor.getColorsScheme().getAttributes(SyncEditModeColors.ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES);
            _activeRangeBoxHighlighter =
            _activeEditor.getMarkupModel().addRangeHighlighter(rangeStart, rangeEnd, HighlighterLayer.SELECTION - 3,
                                                               rangeAttributes,
                                                               HighlighterTargetArea.EXACT_RANGE);
            _activeRangeBoxHighlighter.setGreedyToLeft(true);
            _activeRangeBoxHighlighter.setGreedyToRight(true);
//      _activeEditor.getCaretModel().addCaretListener(_caretListener);
            _activeEditor.getDocument().addDocumentListener(_documentListener);
            installEditorActionHandlers();
        }
        catch (RuntimeException e)
        {
            // make sure to not leave editor in a weird state
            leaveSyncEditMode();
            throw e;
        }
    }



    private static void installEditorActionHandlers()
    {
        ActionUtils.installActionHandlerOverride(IdeActions.ACTION_EDITOR_ESCAPE, _escapeHandler);
        ActionUtils.installActionHandlerOverride(IdeActions.ACTION_EDITOR_ENTER, _enterHandler);
        ActionUtils.disableOtherActionsOnSameKeystrokes("NextSyncEditableWordAction");
        ActionUtils.disableOtherActionsOnSameKeystrokes("PreviousSyncEditableWordAction");
//    ActionUtils.installActionHandlerOverride(IdeActions.ACTION_EDITOR_TAB, new EditorActionHandler()
//    {
//      public boolean isEnabled(Editor editor, DataContext dataContext)
//      {
//        return false;
//      }
//
//      public void execute(Editor editor, DataContext dataContext)
//      {
//      }
//    });
//
//    Keymap activeKeymap = KeymapManager.getInstance().getActiveKeymap();
//    _tabActionIDs = activeKeymap.getActionIds(NEXT_WORD_KEYSTROKE);
//    for (int i = 0; i < _tabActionIDs.length; i++) {
//      String tabActionID = _tabActionIDs[i];
//      if (!tabActionID.equals("NextSyncEditableWordAction"))
//        activeKeymap.removeShortcut(tabActionID, new KeyboardShortcut(NEXT_WORD_KEYSTROKE, null));
//    }

//    ActionManager actionManager = ActionManager.getInstance();
//    EditorAction tabAction = (EditorAction)actionManager.getAction(IdeActions.ACTION_EDITOR_TAB);
//    _oldTabHandler = tabAction.getHandler();
//    tabAction.setupHandler(_tabHandler);
    }



    public static boolean isInSyncEditMode(Editor editor)
    {
        return _activeEditor == editor;
    }



    public static void leaveSyncEditMode()
    {
        if (_activeEditor != null)
        {
            try
            {
                clearSyncEditSelection();
                clearWords();
            }
            finally
            {
                try
                {
                    if (_activeRangeBoxHighlighter != null)
                    {
                        _activeEditor.getMarkupModel().removeHighlighter(_activeRangeBoxHighlighter);
                    }
//          _activeEditor.getCaretModel().removeCaretListener(_caretListener);
                    _activeEditor.getDocument().removeDocumentListener(_documentListener);
                    _activeEditor = null;
                    _activeRangeBoxHighlighter = null;
                }
                finally
                {
                    restoreEditorActionHandlers();
                }
            }
        }
    }



    private static void restoreEditorActionHandlers()
    {
        ActionUtils.restoreOriginalActionHandler(IdeActions.ACTION_EDITOR_ESCAPE);
        ActionUtils.restoreOriginalActionHandler(IdeActions.ACTION_EDITOR_ENTER);
        ActionUtils.enableOtherActionsOnSameKeystrokes("NextSyncEditableWordAction");
        ActionUtils.enableOtherActionsOnSameKeystrokes("PreviousSyncEditableWordAction");

//    Keymap activeKeymap = KeymapManager.getInstance().getActiveKeymap();
//    for (int i = 0; i < _tabActionIDs.length; i++) {
//      String tabActionID = _tabActionIDs[i];
//      if (!tabActionID.equals("NextSyncEditableWordAction"))
//        activeKeymap.addShortcut(tabActionID, new KeyboardShortcut(NEXT_WORD_KEYSTROKE, null));
//    }
    }



    public static RangeHighlighter getActiveRangeBoxHighlighter()
    {
        return _activeRangeBoxHighlighter;
    }



    private static void beforeActiveEditorDocumentChange(DocumentEvent e)
    {
        UndoManager undoManager = UndoManager.getInstance(_activeEditor.getProject());
        if (!_modifyingDocument && !undoManager.isUndoInProgress() && !undoManager.isRedoInProgress())
        {
            int blockStart = e.getOffset();
            if (blockStart < getActiveRangeBoxHighlighter().getStartOffset() ||
            blockStart > getActiveRangeBoxHighlighter().getEndOffset())
            {
                return;
            }
            RangeHighlighter selectedWordBoxHighlight = getSelectedWordBoxHighlight();
            if (blockStart + e.getOldLength() > getActiveRangeBoxHighlighter().getEndOffset())
            {
                if (selectedWordBoxHighlight != null)
                {
                    clearSyncEditSelection();
                }
            }
            boolean needsHighlight = false;
            if (selectedWordBoxHighlight == null)
            {
                needsHighlight = true;
            }
            else
            {
                int boxHighlightEnd = selectedWordBoxHighlight.getEndOffset();
                int boxHighlightStart = selectedWordBoxHighlight.getStartOffset();
//        if (boxHighlightEnd == boxHighlightStart)
//          boxHighlightEnd++; // make greedy
                if (blockStart < boxHighlightStart || blockStart > boxHighlightEnd ||
                blockStart + e.getOldLength() > boxHighlightEnd)
                {
                    needsHighlight = true;
                }
            }
            if (needsHighlight)
            {
                if (e.getOldLength() > 1)
                {
                    String overwrittenRange = e.getOldFragment().toString();
                    int overwrittenOffset = e.getOffset();
                    activateSyncEditSelection(_activeEditor, overwrittenRange, overwrittenOffset,
                                              overwrittenOffset + overwrittenRange.length());
                }
                else
                {
                    activateSyncEditSelectionForCaretLocation(_activeEditor);
                }
            }
        }
    }



    public static void activateSyncEditSelectionForCaretLocation(Editor editor)
    {
        Fragment fragment = getSyncEditFragmentAtCaret(editor);
        activateSyncEditSelection(editor, fragment.getText(), fragment.getStart(), fragment.getEnd());
    }



    private static Fragment getSyncEditFragmentAtCaret(Editor editor)
    {
        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection())
        {
            return getFragmentForSelection(editor);
        }
        else
        {
            try
            {
                CaretModel caretModel = editor.getCaretModel();
                int origCaretPos = caretModel.getOffset();
                Fragment fragment = null;
                if (caretModel.getLogicalPosition().column > 0)
                {
                    caretModel.moveToOffset(origCaretPos - 1);
                    selectionModel.selectWordAtCaret(true);
                    caretModel.moveToOffset(origCaretPos);
                    fragment = getFragmentForSelection(editor);
                }
                if (fragment == null || fragment.getEnd() != origCaretPos)
                {
                    selectionModel.removeSelection();
                    selectionModel.selectWordAtCaret(true);
                    fragment = getFragmentForSelection(editor);
                }
                return fragment;
            }
            finally
            {
                selectionModel.removeSelection();
            }
        }
    }



    private static Fragment getFragmentForSelection(Editor editor)
    {
        Fragment fragment = new Fragment();
        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection())
        {
            int start = selectionModel.getSelectionStart();
            int end = selectionModel.getSelectionEnd();
            if (start > end)
            {
                int temp = end;
                end = start;
                start = temp;
            }
            fragment.setStart(start);
            fragment.setEnd(end);
            if (start != end)
            {
                fragment.setText(editor.getDocument().getCharsSequence().subSequence(start, end).toString());
            }
        }
        return fragment;
    }



    private static boolean activateSyncEditSelection(Editor editor, String selectedWord, int syncEditStart, int syncEditEnd)
    {
        RangeHighlighter rangeHighlighter = getActiveRangeBoxHighlighter();
        int rangeStart = rangeHighlighter.getStartOffset();
        int rangeEnd = rangeHighlighter.getEndOffset();
        if (syncEditStart >= rangeStart && syncEditEnd <= rangeEnd)
        {
            clearSyncEditSelection();
            TextRange[] matchingWordRanges = EditorUtils.findMatchingWordRanges(editor, rangeStart, rangeEnd,
                                                                                selectedWord);
            if (matchingWordRanges.length > 1)
            {
                activateSyncEditSelection(editor, syncEditStart, syncEditEnd, matchingWordRanges);
                return true;
            }
        }
        return false;
    }



    private static void activateSyncEditSelection(Editor editor, int wordStart, int wordEnd, TextRange[] matchingWordRanges)
    {
        if (_hasSyncEditSelection)
        {
            clearSyncEditSelection();
        }
        try
        {
            highlightMatchingWordInstances(editor, matchingWordRanges, wordStart);
            _hasSyncEditSelection = true;
            installSelectionActionHandlers();
            _selectedWordBoxHighlight =
            editor.getMarkupModel().addRangeHighlighter(wordStart, wordEnd, HighlighterLayer.SELECTION - 2,
                                                        new TextAttributes(null, null, Color.red,
                                                                           EffectType.BOXED,
                                                                           Font.PLAIN),
                                                        HighlighterTargetArea.EXACT_RANGE);
            _selectedWordBoxHighlight.setGreedyToLeft(true);
            _selectedWordBoxHighlight.setGreedyToRight(true);
        }
        catch (RuntimeException e)
        {
            // make sure not to leave editor in weird state
            clearSyncEditSelection();
            throw e;
        }
    }



    private static void installSelectionActionHandlers()
    {
        ActionUtils.installActionHandlerOverride(IdeActions.ACTION_EDITOR_MOVE_LINE_START, _homeHandler);
        ActionUtils.installActionHandlerOverride(ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION,
                                                 _homeWithSelectionHandler);
        ActionUtils.installActionHandlerOverride(IdeActions.ACTION_EDITOR_MOVE_LINE_END, _endHandler);
        ActionUtils.installActionHandlerOverride(ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION, _endWithSelectionHandler);
    }



    static void highlightMatchingWordInstances(Editor editor, TextRange[] matchingWordRanges, int selectedWordPosition)
    {
        for (int i = 0; i < matchingWordRanges.length; i++)
        {
            TextRange matchingWordRange = matchingWordRanges[i];
            TextAttributes textAttributes = EditorColors.SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes();
            int startOffset = matchingWordRange.getStartOffset();
            if (startOffset == selectedWordPosition)
            {
                textAttributes = EditorColors.WRITE_SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes();
            }
            RangeHighlighter matchingWordHighlighter = editor.getMarkupModel()
                                                             .addRangeHighlighter(startOffset,
                                                                                  matchingWordRange.getEndOffset(),
                                                                                  HighlighterLayer.SELECTION - 1,
                                                                                  textAttributes,
                                                                                  HighlighterTargetArea.EXACT_RANGE);
            matchingWordHighlighter.setGreedyToLeft(true);
            matchingWordHighlighter.setGreedyToRight(true);
            _matchingWordColorHighlighters.add(matchingWordHighlighter);
            if (startOffset == selectedWordPosition)
            {
                _selectedWordColorHighlight = matchingWordHighlighter;
            }
        }
    }



    private static void activeEditorDocumentChange(DocumentEvent e)
    {
        UndoManager undoManager = UndoManager.getInstance(_activeEditor.getProject());
        if (!_modifyingDocument && !undoManager.isUndoInProgress() && !undoManager.isRedoInProgress())
        {
            if (getSelectedWordBoxHighlight() != null)
            {
                int editOffset = e.getOffset();
                int boxStartOffset = getSelectedWordBoxHighlight().getStartOffset();
                int boxEndOffset = getSelectedWordBoxHighlight().getEndOffset();
                if (editOffset >= boxStartOffset && editOffset <= boxEndOffset)
                {
                    handleSyncEdit(e);
                }
            }
        }
    }



    private static void handleSyncEdit(DocumentEvent e)
    {
        _modifyingDocument = true;
        try
        {
            int caretOffset = _activeEditor.getCaretModel().getOffset();
            final int subOffset = e.getOffset() - getSelectedWordBoxHighlight().getStartOffset();
            int lengthRemoved = e.getOldLength();
            if (lengthRemoved > 0)
            {
                for (int i = _matchingWordColorHighlighters.size() - 1; i >= 0; i--)
                {
                    RangeHighlighter rangeHighlighter = (RangeHighlighter) _matchingWordColorHighlighters.get(i);
                    if (rangeHighlighter != _selectedWordColorHighlight)
                    {
                        int otherOffset = rangeHighlighter.getStartOffset() + subOffset;
                        _activeEditor.getDocument().deleteString(otherOffset, otherOffset + lengthRemoved);
                    }
                }
            }
            int lengthAdded = e.getNewLength();
            int caretOffsetCorrection = 0;
            if (lengthAdded > 0)
            {
                for (int i = _matchingWordColorHighlighters.size() - 1; i >= 0; i--)
                {
                    final RangeHighlighter rangeHighlighter = (RangeHighlighter) _matchingWordColorHighlighters.get(i);
                    if (rangeHighlighter != _selectedWordColorHighlight)
                    {
                        final int otherOffset = rangeHighlighter.getStartOffset() + subOffset;
                        _activeEditor.getDocument().insertString(otherOffset, e.getNewFragment());
                        if (otherOffset < caretOffset)
                        {
                            caretOffsetCorrection += lengthAdded;
                        }
                    }
                }
            }
            if (caretOffsetCorrection != 0)
            {
                final int finalCaretOffsetCorrection = caretOffsetCorrection;
                //              int oldOffset = _activeEditor.getCaretModel().getOffset();
                //              int newOffset = oldOffset + finalCaretOffsetCorrection;
                //                _activeEditor.getCaretModel().moveCaretRelatively(finalCaretOffsetCorrection, 0, false, false, true);
                //                _activeEditor.getCaretModel().moveToOffset(newOffset);
                //                _activeEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        int oldOffset = _activeEditor.getCaretModel().getOffset();
                        int newOffset = oldOffset + finalCaretOffsetCorrection;
                        _activeEditor.getCaretModel().moveToOffset(newOffset);
                        _activeEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                        //                    System.out.println(
                        //                      "SyncEditModeController.run : finalCaretOffsetCorrection = " + finalCaretOffsetCorrection);
                    }
                });
            }
        }
        finally
        {
            _modifyingDocument = false;
            clearWords();
        }
    }



    private static void doEscape()
    {
        if (_hasSyncEditSelection)
        {
            clearSyncEditSelection();
        }
        else
        {
            leaveSyncEditMode();
        }
    }



    public static void clearSyncEditSelection()
    {
        if (_hasSyncEditSelection)
        {
            _hasSyncEditSelection = false;
            restoreSelectionActionHandlers();
            _selectedWordBoxHighlight = null;
            for (int i = 0; i < _matchingWordColorHighlighters.size(); i++)
            {
                RangeHighlighter rangeHighlighter = (RangeHighlighter) _matchingWordColorHighlighters.get(i);
                _activeEditor.getMarkupModel().removeHighlighter(rangeHighlighter);
            }
            _matchingWordColorHighlighters.clear();
            _selectedWordColorHighlight = null;
        }
    }



    private static void restoreSelectionActionHandlers()
    {
        ActionUtils.restoreOriginalActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_START);
        ActionUtils.restoreOriginalActionHandler(IdeActions.ACTION_EDITOR_MOVE_LINE_END);
        ActionUtils.restoreOriginalActionHandler(ACTION_EDITOR_MOVE_LINE_START_WITH_SELECTION);
        ActionUtils.restoreOriginalActionHandler(ACTION_EDITOR_MOVE_LINE_END_WITH_SELECTION);
        if (_selectedWordBoxHighlight != null)
        {
            _activeEditor.getMarkupModel().removeHighlighter(_selectedWordBoxHighlight);
        }
    }



    public static RangeHighlighter getSelectedWordBoxHighlight()
    {
        return _selectedWordBoxHighlight;
    }



    public static boolean isInSyncEditMode()
    {
        return getActiveEditor() != null;
    }



    public static Editor getActiveEditor()
    {
        return _activeEditor;
    }



    public static void selectNextRepeatedWord()
    {
        if (_activeEditor != null)
        {
            List<Word> words = getRepeatedWords();
            Word wordToSelect = null;
            if (!words.isEmpty())
            {
                if (_selectedRepeatedWordIndex == -1)
                {
                    // find the next word
                    wordToSelect = Word.findNextWord(words, _activeEditor.getCaretModel().getOffset());
                    if (wordToSelect == null)
                    {
                        // start search from range start
                        wordToSelect = Word.findNextWord(words, getActiveRangeStartOffset());
                    }
                    if (wordToSelect != null)
                    {
                        _selectedRepeatedWordIndex = words.indexOf(wordToSelect);
                    }
                }
                else
                {
                    _selectedRepeatedWordIndex++;
                    if (_selectedRepeatedWordIndex == words.size())
                    {
                        _selectedRepeatedWordIndex = 0;
                    }
                    if (_selectedRepeatedWordIndex < words.size())
                    {
                        wordToSelect = words.get(_selectedRepeatedWordIndex);
                    }
                }
            }
            if (wordToSelect != null)
            {
                selectRepeatedWord(_activeEditor, wordToSelect);
            }
        }
    }



    public static void selectLastRepeatedWord()
    {
        if (_activeEditor != null)
        {
            List<Word> words = getRepeatedWords();
            Word wordToSelect = null;
            if (!words.isEmpty())
            {
                if (_selectedRepeatedWordIndex == -1)
                {
                    // find the previous word
                    wordToSelect = Word.findLastWord(words, _activeEditor.getCaretModel().getOffset());
                    if (wordToSelect == null)
                    {
                        // start search from range start
                        wordToSelect = Word.findLastWord(words, getActiveRangeEndOffset());
                    }
                    if (wordToSelect != null)
                    {
                        _selectedRepeatedWordIndex = words.indexOf(wordToSelect);
                    }
                }
                else
                {
                    _selectedRepeatedWordIndex--;
                    if (_selectedRepeatedWordIndex == -1)
                    {
                        _selectedRepeatedWordIndex = words.size() - 1;
                    }
                    if (_selectedRepeatedWordIndex > -1)
                    {
                        wordToSelect = words.get(_selectedRepeatedWordIndex);
                    }
                }
            }
            if (wordToSelect != null)
            {
                selectRepeatedWord(_activeEditor, wordToSelect);
            }
        }
    }



    public static void selectRepeatedWord(Editor editor, Word wordToSelect)
    {
        TextRange firstInstanceRange = wordToSelect.getFirstInstanceRange();
        TextRange[] subsequentRanges = wordToSelect.getSubsequentInstanceRanges();
        TextRange[] allInstanceRanges = new TextRange[subsequentRanges.length + 1];
        allInstanceRanges[0] = firstInstanceRange;
        System.arraycopy(subsequentRanges, 0, allInstanceRanges, 1, subsequentRanges.length);
        activateSyncEditSelection(editor, firstInstanceRange.getStartOffset(), firstInstanceRange.getEndOffset(),
                                  allInstanceRanges);
        editor.getSelectionModel().setSelection(firstInstanceRange.getStartOffset(),
                                                firstInstanceRange.getEndOffset());
        editor.getCaretModel().moveToOffset(firstInstanceRange.getEndOffset());
    }



    private static List<Word> getRepeatedWords()
    {
        if (_repeatedWords == null)
        {
            //noinspection NonThreadSafeLazyInitialization
            _repeatedWords = new ArrayList<Word>();
            _words = new LinkedList<Word>();
            int offset = getActiveRangeStartOffset();
            while (offset < getActiveRangeEndOffset())
            {
//        CharSequence charsSequence = _activeEditor.getDocument().getCharsSequence();
//        System.out.println(charsSequence.subSequence(offset - 15, offset) + "|" + charsSequence.subSequence(offset, offset + 15));
                TextRange[] wordRanges = EditorUtils.getWordsAtOffset(_activeEditor, offset);
                int limit = shouldFindCompoundWords() ? Math.min(wordRanges.length, 1) : wordRanges.length;
                for (int i = 0; (i < limit); i++)
                {
                    TextRange wordRange = wordRanges[i];
                    boolean isWordPart = i == 0;
                    if (wordRange.getStartOffset() >= getActiveRangeStartOffset() &&
                    wordRange.getEndOffset() <= getActiveRangeEndOffset())
                    {
                        addWord(wordRange, isWordPart);
                    }
                }
                int wordLength = 1;
//        if (wordRanges.length > 0) {
//          if (offset == wordRanges[0].getStartOffset() && wordRanges[0].getLength() > 0) {
//            wordLength = wordRanges[0].getLength();
//          }
//        }
                offset += wordLength;
            }
        }
//    System.out.println(
//      "SyncEditModeController.getRepeatedWords : _repeatedWords = " + _repeatedWords);
        return _repeatedWords;
    }



    private static void addWord(TextRange wordRange, boolean isWordPart)
    {
        String wordText = _activeEditor.getDocument().getCharsSequence()
                                       .subSequence(wordRange.getStartOffset(), wordRange.getEndOffset()).toString();
        if (shouldFindCompoundWords())
        {
            for (int i = _words.size() - 1; i >= 0; i--)
            {
                Word lastWord = _words.get(i);
                int lastWordEnd = lastWord.getFirstInstanceRange().getEndOffset();
                if (lastWordEnd == wordRange.getStartOffset())
                {
                    addWord(new TextRange(lastWord.getFirstInstanceRange().getStartOffset(), wordRange.getEndOffset()),
                            false);
                }
                else if (lastWordEnd < wordRange.getStartOffset())
                {
                    // no more
                    break;
                }
            }
            _words.add(new Word(wordText, wordRange));
        }
//            System.out.println("** Found Word " + wordText + " at " + wordRange);
        Word word = _wordMap.get(wordText);
        if (word == null)
        {
            TextRange[] matchingWordRanges = EditorUtils.findMatchingWordRanges(_activeEditor, wordRange.getEndOffset(),
                                                                                getActiveRangeEndOffset(), wordText);
            word = new Word(wordText, wordRange, matchingWordRanges, isWordPart);
            _wordMap.put(wordText, word);
            if (word.isRepeated())
            {
                _repeatedWords.add(word);
            }
        }
    }



    private static boolean shouldFindCompoundWords()
    {
        return false;  // todo : incomplete : implement
    }



    private static void clearWords()
    {
        _repeatedWords = null;
        _words = null;
        _selectedRepeatedWordIndex = -1;
        _wordMap.clear();
    }



    private static int getActiveRangeEndOffset()
    {
        return _activeRangeBoxHighlighter.getEndOffset();
    }



    private static int getActiveRangeStartOffset()
    {
        return _activeRangeBoxHighlighter.getStartOffset();
    }



    private static class Fragment
    {


        private String _text;
        private int _start;
        private int _end;



        public Fragment()
        {
        }



        public Fragment(String text, int start, int end)
        {
            _text = text;
            _start = start;
            _end = end;
        }



        public int getEnd()
        {
            return _end;
        }



        public void setEnd(int end)
        {
            _end = end;
        }



        public int getStart()
        {
            return _start;
        }



        public void setStart(int start)
        {
            _start = start;
        }



        public String getText()
        {
            return _text;
        }



        public void setText(String text)
        {
            _text = text;
        }
    }

}

