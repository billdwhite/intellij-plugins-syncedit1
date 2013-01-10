package com.anecdote.ideaplugins.syncedit;

import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;

public class SyncEditProjectComponent
implements ProjectComponent
{

    private final Project _project;



    public SyncEditProjectComponent(Project project)
    {
        _project = project;
    }



    public void initComponent()
    {
        // TODO: insert component initialization logic here
    }



    public void disposeComponent()
    {
        // TODO: insert component disposal logic here
    }



    public String getComponentName()
    {
        return "SyncEditIntentionAction";
    }



    public void projectOpened()
    {
        IntentionManager.getInstance(_project).addAction(new SyncEditModeAction());
    }



    public void projectClosed()
    {
        // called when project is being closed
    }
}
