package com.anecdote.ideaplugins.syncedit;

import com.anecdote.ideaplugins.util.PluginColorSettingsPage;
import com.anecdote.ideaplugins.util.PluginColorSettingsPageImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.options.colors.ColorSettingsPages;

public class SyncEditApplicationComponent
implements ApplicationComponent
{

    private static final String DEMO_TEXT = "The quick brown fox jumped over the lazy dog.  <sync>This area of text is in SyncEdit mode.  \n" +
                                            "This means that any modifications to words in this area will cause identical modifications in \n" +
                                            "all other matching words within the area.  See the SyncEdit plugin description for more \n" +
                                            "information.</sync>  The quick brown fox jumped over the lazy dog. ";



    public SyncEditApplicationComponent()
    {
    }



    public void initComponent()
    {
        PluginColorSettingsPageImpl.register();
        AttributesDescriptor activeSyncEditRangeAttributesDescriptor =
        new AttributesDescriptor("SyncEdit active range", SyncEditModeColors.ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES);
        ColorSettingsPage[] colorSettingsPages = ColorSettingsPages.getInstance().getRegisteredPages();
        for (int i = 0; i < colorSettingsPages.length; i++)
        {
            ColorSettingsPage colorSettingsPage = colorSettingsPages[i];
            if (colorSettingsPage instanceof PluginColorSettingsPage)
            {
                PluginColorSettingsPage pluginsColorSettingsPage = (PluginColorSettingsPage) colorSettingsPage;
                pluginsColorSettingsPage.registerPluginColorSettings("SyncEdit", null, new AttributesDescriptor[]{
                activeSyncEditRangeAttributesDescriptor
                }, DEMO_TEXT);
                pluginsColorSettingsPage.getAdditionalHighlightingTagToDescriptorMap().put("sync",
                                                                                           SyncEditModeColors.ACTIVE_SYNC_EDIT_RANGE_ATTRIBUTES);
            }
        }
    }



    public void disposeComponent()
    {
        // TODO: insert component disposal logic here
    }



    public String getComponentName()
    {
        return "SyncEditApplicationComponent";
    }
}
