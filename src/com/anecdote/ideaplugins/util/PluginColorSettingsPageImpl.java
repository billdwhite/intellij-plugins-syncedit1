package com.anecdote.ideaplugins.util;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPages;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PluginColorSettingsPageImpl
implements PluginColorSettingsPage
{

    private AttributesDescriptor[] _attributeDescriptors;
    private Map<String, AttributesDescriptor> _attributeDescriptorMap = new TreeMap<String, AttributesDescriptor>();
    private ColorDescriptor[] _colorDescriptors;
    private Map<String, ColorDescriptor> _colorDescriptorMap = new TreeMap<String, ColorDescriptor>();
    private String _demoText = null;
    private List<String> _demoTextFragments = new ArrayList<String>();
    private static final String FOOTNOTE =
    "<info>This Color Settings Panel has been designed as a central place for any colour settings \n" +
    "that are needed to be defined by Plug-ins.  The current implementations of ColorSettingsPanel\n" +
    " provided in the OpenAPI cannot be easily extended by Plug-ins, but this is an implementation \n" +
    "that can.  Unfortunately plugins each have their own classloader so that it is currently not \n" +
    "possible to include color settings for your plugin on this tab.  Vote for request 7636 at \n" +
    "http://www.jetbrains.net/jira/browse/IDEA-7636 to support this.";
    private Map<String, TextAttributesKey> _highlightingMap = new HashMap<String, TextAttributesKey>();



    {
        _highlightingMap.put("info", TextAttributesKey.createTextAttributesKey("PCSP_INFO_TEXT",
                                                                               new TextAttributes(null,
                                                                                                  null,
                                                                                                  null,
                                                                                                  null,
                                                                                                  Font.ITALIC)));
    }



    public static void register()
    {
        ColorSettingsPages.getInstance().registerPage(new PluginColorSettingsPageImpl());
    }



    private PluginColorSettingsPageImpl()
    {
    }



    @NotNull
    public String getDisplayName()
    {
        return "Plug-ins";
    }



    @Nullable
    public Icon getIcon()
    {
        return new ImageIcon(getClass().getResource("/com/anecdote/ideaplugins/util/plugincolors.png"));
    }



    public void registerPluginColorSettings(String pluginID, ColorDescriptor[] colorDescriptors,
                                            AttributesDescriptor[] attributeDescriptors, String demoText)
    {
        if (colorDescriptors != null)
        {
            _colorDescriptors = null;
            for (int i = 0; i < colorDescriptors.length; i++)
            {
                ColorDescriptor colorDescriptor = colorDescriptors[i];
                _colorDescriptorMap.put(pluginID + "." + colorDescriptor.getDisplayName(), colorDescriptor);
            }
        }
        if (attributeDescriptors != null)
        {
            _attributeDescriptors = null;
            for (int i = 0; i < attributeDescriptors.length; i++)
            {
                AttributesDescriptor attributesDescriptor = attributeDescriptors[i];
                _attributeDescriptorMap.put(pluginID + "." + attributesDescriptor.getDisplayName(),
                                            attributesDescriptor);
            }
        }
        if (demoText != null)
        {
            _demoTextFragments.add(demoText);
            _demoText = null;
        }
    }



    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors()
    {
        if (_attributeDescriptors == null)
        {
            Collection<AttributesDescriptor> attributesDescriptors = _attributeDescriptorMap.values();
            _attributeDescriptors = attributesDescriptors.toArray(new AttributesDescriptor[attributesDescriptors.size()]);
        }
        return _attributeDescriptors;
    }



    @NotNull
    public ColorDescriptor[] getColorDescriptors()
    {
        if (_colorDescriptors == null)
        {
            Collection<ColorDescriptor> colorDescriptors = _colorDescriptorMap.values();
            _colorDescriptors = colorDescriptors.toArray(new ColorDescriptor[colorDescriptors.size()]);
        }
        return _colorDescriptors;
    }



    @NotNull
    public SyntaxHighlighter getHighlighter()
    {
        return new PlainSyntaxHighlighter();
    }



    @NonNls
    @NotNull
    public String getDemoText()
    {
        if (_demoText == null)
        {
            _demoText = "";
            for (int i = 0; i < _demoTextFragments.size(); i++)
            {
                String s = _demoTextFragments.get(i);
                _demoText += s + "\n\n";
            }
            _demoText += FOOTNOTE;
        }
        return _demoText;
    }



    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap()
    {
        return _highlightingMap;
    }
}
