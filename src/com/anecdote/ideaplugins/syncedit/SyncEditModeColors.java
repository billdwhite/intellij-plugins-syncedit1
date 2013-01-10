package com.anecdote.ideaplugins.syncedit;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.awt.*;

public class SyncEditModeColors
{

    private static final TextAttributes DEFAULT_ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES =
        new TextAttributes(null,
                           null,
                           EditorColors.SEARCH_RESULT_ATTRIBUTES.getDefaultAttributes().getBackgroundColor(),
                           EffectType.BOXED,
                           Font.PLAIN);


    public static final TextAttributesKey ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES =
        TextAttributesKey.createTextAttributesKey("ACTIVE_SYNC_EDIT_RANGE",
                                                  DEFAULT_ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES);



    private SyncEditModeColors()
    {
    }
}
