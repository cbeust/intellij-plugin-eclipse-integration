package com.javaexpert.intellij.plugins.eclipseclasspath.synchronizer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * User: piotrga
 * Date: 2006-12-03
 * Time: 10:08:29
 */
public class UI {
    void displayInformationDialog(String[] urls) {
        String res = "";
        for (String url : urls) {
            res += url + "\n";
        }
        Messages.showMessageDialog(
                "Added the following libs:\n" + res, "Eclipse Dependencies Update", Messages.getInformationIcon());
    }

    void displayNoProjectSelectedWarnning() {
        Messages.showWarningDialog("Please open any project.", "No open projects");
    }

    String getLibraryNameFromUser(Project project, String defaultLibraryName) {
        return Messages.showInputDialog(project, "Please enter library name.", "Creating library for Eclipse dependencies", Messages.getQuestionIcon(), defaultLibraryName, null);
    }
}
