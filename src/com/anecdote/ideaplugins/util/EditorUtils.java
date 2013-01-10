package com.anecdote.ideaplugins.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.TextRange;

import java.util.ArrayList;
import java.util.List;

public class EditorUtils
{

    private EditorUtils()
    {
    }



    public static TextRange[] getWordsAtOffset(Editor editor, int offset)
    {
        Document document = editor.getDocument();
        CharSequence charsequence = document.getCharsSequence();
        if (offset == document.getTextLength())
        {
            offset--;
        }
        return getWordSelections(editor, charsequence, offset);
    }



    private static TextRange[] getWordSelections(Editor editor, CharSequence charsequence, int offset)
    {
        ArrayList<TextRange> arraylist = new ArrayList<TextRange>();
        addWordSelection(editor.getSettings().isCamelWords(), charsequence, offset, arraylist);
        return arraylist.toArray(new TextRange[arraylist.size()]);
    }



    // copied from SelectWordUtil
    public static void addWordSelection(boolean flag, CharSequence charsequence, int i, List list)
    {
        TextRange textrange = flag ? a(charsequence, i) : null;
        if (textrange != null)
        {
            list.add(textrange);
        }
        TextRange textrange1 = b(charsequence, i);
        if (textrange1 != null && !textrange1.equals(textrange))
        {
            list.add(textrange1);
        }
    }



    private static TextRange a(CharSequence charsequence, int i)
    {
        if (i > 0 && !Character.isJavaIdentifierPart(charsequence.charAt(i)) &&
        Character.isJavaIdentifierPart(charsequence.charAt(i - 1)))
        {
            i--;
        }
        if (Character.isJavaIdentifierPart(charsequence.charAt(i)))
        {
            int j = i;
            int k = i + 1;
            int l = charsequence.length();
            do
            {
                if (j <= 0 || !Character.isJavaIdentifierPart(charsequence.charAt(j - 1)))
                {
                    break;
                }
                char c = charsequence.charAt(j - 1);
                char c2 = charsequence.charAt(j);
                char c4 = j + 1 >= l ? '\0' : charsequence.charAt(j + 1);
                if (Character.isLowerCase(c) && Character.isUpperCase(c2) || c == '_' && c2 != '_' ||
                Character.isUpperCase(c) && Character.isUpperCase(c2) && Character.isLowerCase(c4))
                {
                    break;
                }
                j--;
            } while (true);
            do
            {
                if (k >= l || !Character.isJavaIdentifierPart(charsequence.charAt(k)))
                {
                    break;
                }
                char c1 = charsequence.charAt(k - 1);
                char c3 = charsequence.charAt(k);
                char c5 = k + 1 >= l ? '\0' : charsequence.charAt(k + 1);
                if (Character.isLowerCase(c1) && Character.isUpperCase(c3) || c1 != '_' && c3 == '_' ||
                Character.isUpperCase(c1) && Character.isUpperCase(c3) && Character.isLowerCase(c5))
                {
                    break;
                }
                k++;
            } while (true);
            if (j + 1 < k)
            {
                return new TextRange(j, k);
            }
        }
        return null;
    }



    private static TextRange b(CharSequence charsequence, int i)
    {
        if (charsequence.length() == 0)
        {
            return null;
        }
        if (i > 0 && !Character.isJavaIdentifierPart(charsequence.charAt(i)) &&
        Character.isJavaIdentifierPart(charsequence.charAt(i - 1)))
        {
            i--;
        }
        if (Character.isJavaIdentifierPart(charsequence.charAt(i)))
        {
            int j = i;
            int k = i;
            for (; j > 0 && Character.isJavaIdentifierPart(charsequence.charAt(j - 1)); j--)
            {
                ;
            }
            for (; k < charsequence.length() && Character.isJavaIdentifierPart(charsequence.charAt(k)); k++)
            {
                ;
            }
            return new TextRange(j, k);
        }
        else
        {
            return null;
        }
    }



    public static void moveCaretForwardTo(Editor editor, int toOffset, boolean withSelection)
    {
        CaretModel caretModel = editor.getCaretModel();
        SelectionModel selectionModel = editor.getSelectionModel();
        int caretOffset = caretModel.getOffset();
        if (withSelection)
        {
            int newSelectionStart = caretOffset;
            int newSelectionEnd = toOffset;
            if (selectionModel.hasSelection())
            {
                int selectionStart = selectionModel.getSelectionStart();
                int selectionEnd = selectionModel.getSelectionEnd();
                if (selectionStart < caretOffset)
                { // extension of selection to right
                    newSelectionStart = selectionStart;
                }
                else
                { // inverting of selection to right
                    if (selectionEnd < toOffset)
                    {
                        newSelectionStart = selectionEnd;
                    }
                    else if (selectionEnd == toOffset)
                    {
                        newSelectionStart = toOffset;
                    }
                    else
                    { // selection ends after selected word - retain end
                        newSelectionStart = toOffset;
                        newSelectionEnd = selectionEnd;
                    }
                }
            }
            selectionModel.setSelection(newSelectionStart, newSelectionEnd);
        }
        else
        {
            selectionModel.removeSelection();
        }
        caretModel.moveToOffset(toOffset);
    }



    public static void moveCaretBackTo(Editor editor, int toOffset, boolean withSelection)
    {
        CaretModel caretModel = editor.getCaretModel();
        SelectionModel selectionModel = editor.getSelectionModel();
        int caretOffset = caretModel.getOffset();
        if (withSelection)
        {
            int newSelectionStart = toOffset;
            int newSelectionEnd = caretOffset;
            if (selectionModel.hasSelection())
            {
                int selectionStart = selectionModel.getSelectionStart();
                int selectionEnd = selectionModel.getSelectionEnd();
                if (selectionEnd > caretOffset)
                { // extension of selection to left
                    newSelectionEnd = selectionEnd;
                }
                else
                { // inverting of selection to left
                    if (selectionStart > toOffset)
                    {
                        newSelectionEnd = selectionStart;
                    }
                    else if (selectionStart == toOffset)
                    {
                        newSelectionEnd = toOffset;
                    }
                    else
                    { // selection starts before selected word - retain start
                        newSelectionStart = selectionStart;
                        newSelectionEnd = toOffset;
                    }
                }
            }
            selectionModel.setSelection(newSelectionStart, newSelectionEnd);
        }
        else
        {
            selectionModel.removeSelection();
        }
        caretModel.moveToOffset(toOffset);
    }



    public static TextRange[] findMatchingWordRanges(Editor editor, int rangeStart, int rangeEnd, String selectedWord)
    {
        List<TextRange> result = new ArrayList<TextRange>();
        String activeRangeText =
        editor.getDocument().getCharsSequence().subSequence(rangeStart, rangeEnd).toString();
        int wordLength = selectedWord.length();
        int cursor = 0;
        while (cursor >= 0 && cursor < activeRangeText.length())
        {
            cursor = activeRangeText.indexOf(selectedWord, cursor);
            if (cursor > -1)
            {
                result.add(new TextRange(cursor + rangeStart, rangeStart + cursor + wordLength));
                cursor += wordLength;
            }
        }
        return result.toArray(new TextRange[result.size()]);
    }



    public static Editor getEditor(AnActionEvent e)
    {
        return (Editor) e.getDataContext().getData(DataConstants.EDITOR);
    }
}
