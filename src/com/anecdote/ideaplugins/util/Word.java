package com.anecdote.ideaplugins.util;

import com.intellij.openapi.util.TextRange;

import java.util.List;

public class Word
{

    private final String _wordText;
    private final TextRange _firstInstanceRange;
    private final TextRange[] _subsequentInstanceRanges;
    private final boolean _wordPart;



    public Word(String wordText, TextRange firstInstanceRange, TextRange[] subsequentInstanceRanges, boolean wordPart)
    {
        _wordText = wordText;
        _firstInstanceRange = firstInstanceRange;
        _subsequentInstanceRanges = subsequentInstanceRanges;
        _wordPart = wordPart;
    }



    public Word(String wordText, TextRange wordRange)
    {
        this(wordText, wordRange, null, true);
    }



    public TextRange getFirstInstanceRange()
    {
        return _firstInstanceRange;
    }



    public TextRange[] getSubsequentInstanceRanges()
    {
        return _subsequentInstanceRanges;
    }



    public String getWordText()
    {
        return _wordText;
    }



    public boolean isWordPart()
    {
        return _wordPart;
    }



    public boolean isRepeated()
    {
        return _subsequentInstanceRanges.length > 0;
    }



    public static Word findLastWord(List<Word> words, int fromOffset)
    {
        Word wordToSelect = null;
        for (int i = words.size() - 1; i >= 0; i--)
        {
            Word word = words.get(i);
            if (word.getFirstInstanceRange().getStartOffset() <= fromOffset)
            {
                wordToSelect = word;
            }
        }
        return wordToSelect;
    }



    public static Word findNextWord(List<Word> words, int fromOffset)
    {
        Word wordToSelect = null;
        for (int i = 0; i < words.size() && wordToSelect == null; i++)
        {
            Word word = words.get(i);
            if (word.getFirstInstanceRange().getStartOffset() >= fromOffset)
            {
                wordToSelect = word;
            }
        }
        return wordToSelect;
    }



    public String toString()
    {
        return _wordText;
    }
}
