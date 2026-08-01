package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.utils.GT;

final class UserInformationDialog extends Dialog<Void> {

  UserInformationDialog(final Window owner, final ConnectedUser user) {
    super();
    initOwner(owner);
    setTitle(GT._T("User information"));
    setHeaderText(null);

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(15, 15, 15, 15));

    final Label usernameLabel = new Label(GT._T("Username:"));
    final TextField usernameField = new TextField(user.username());
    usernameField.setEditable(false);
    grid.add(usernameLabel, 0, 0);
    grid.add(usernameField, 1, 0);

    final Label wikiLabel = new Label(GT._T("Wiki:"));
    final TextField wikiField = new TextField(user.wiki().toString());
    wikiField.setEditable(false);
    grid.add(wikiLabel, 0, 1);
    grid.add(wikiField, 1, 1);

    final Label groupsLabelField = new Label(GT._T("Groups:"));
    final TextArea groupsField =
        new TextArea(String.join("\n", user.groups().stream().sorted().toList()));
    groupsField.setEditable(false);
    groupsField.setPrefRowCount(4);
    grid.add(groupsLabelField, 0, 2);
    grid.add(groupsField, 1, 2);

    final Label rightsLabelField = new Label(GT._T("Rights:"));
    final TextArea rightsField =
        new TextArea(String.join("\n", user.rights().stream().sorted().toList()));
    rightsField.setEditable(false);
    rightsField.setPrefRowCount(6);
    grid.add(rightsLabelField, 0, 3);
    grid.add(rightsField, 1, 3);

    GridPane.setHgrow(usernameField, Priority.ALWAYS);
    GridPane.setHgrow(wikiField, Priority.ALWAYS);
    GridPane.setHgrow(groupsField, Priority.ALWAYS);
    GridPane.setHgrow(rightsField, Priority.ALWAYS);

    getDialogPane().setContent(grid);
    getDialogPane()
        .getButtonTypes()
        .add(new ButtonType(GT._T("Close"), ButtonBar.ButtonData.CANCEL_CLOSE));
  }
}
