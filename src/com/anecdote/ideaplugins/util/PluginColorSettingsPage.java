package com.anecdote.ideaplugins.util;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface PluginColorSettingsPage
extends ColorSettingsPage
{

    void registerPluginColorSettings(String pluginID, ColorDescriptor[] colorDescriptors,
                                     AttributesDescriptor[] attributeDescriptors, String demoText);

    @NotNull
    Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap();
}
