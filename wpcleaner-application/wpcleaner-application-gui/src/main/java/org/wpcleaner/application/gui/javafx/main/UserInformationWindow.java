package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

final class UserInformationWindow extends JavaFxWindow<JavaFxMainWindowServices> {

  private final ConnectedUser user;

  UserInformationWindow(final JavaFxMainWindowServices services, final ConnectedUser user) {
    this(services, null, user);
  }

  UserInformationWindow(
      final JavaFxMainWindowServices services,
      @Nullable final Stage owner,
      final ConnectedUser user) {
    super(services, owner);
    this.user = Objects.requireNonNull(user);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(GT._T("User information"));
    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "userInformation";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();

    final VBox mainContainer = new VBox(15);
    mainContainer.setPadding(new Insets(15, 15, 15, 15));

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    final ColumnConstraints colLabel = new ColumnConstraints();
    colLabel.setMinWidth(Region.USE_PREF_SIZE);
    colLabel.setHgrow(Priority.NEVER);

    final ColumnConstraints colField = new ColumnConstraints();
    colField.setHgrow(Priority.ALWAYS);

    grid.getColumnConstraints().addAll(colLabel, colField);

    final Label usernameLabel = new Label(GT._T("Username:"));
    usernameLabel.setMinWidth(Region.USE_PREF_SIZE);
    final TextField usernameField = new TextField(user.username());
    usernameField.setEditable(false);
    grid.add(usernameLabel, 0, 0);
    grid.add(usernameField, 1, 0);

    final Label wikiLabel = new Label(GT._T("Wiki:"));
    wikiLabel.setMinWidth(Region.USE_PREF_SIZE);
    final TextField wikiField = new TextField(user.wiki().toString());
    wikiField.setEditable(false);
    grid.add(wikiLabel, 0, 1);
    grid.add(wikiField, 1, 1);

    final Label groupsLabelField = new Label(GT._T("Groups:"));
    groupsLabelField.setMinWidth(Region.USE_PREF_SIZE);
    final TextArea groupsField =
        new TextArea(String.join("\n", user.groups().stream().sorted().toList()));
    groupsField.setEditable(false);
    groupsField.setPrefRowCount(4);
    grid.add(groupsLabelField, 0, 2);
    grid.add(groupsField, 1, 2);

    final Label rightsLabelField = new Label(GT._T("Rights:"));
    rightsLabelField.setMinWidth(Region.USE_PREF_SIZE);
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

    VBox.setVgrow(grid, Priority.ALWAYS);
    GridPane.setVgrow(groupsField, Priority.ALWAYS);
    GridPane.setVgrow(rightsField, Priority.ALWAYS);

    final Button closeButton = new Button(GT._T("Close"));
    closeButton.setCancelButton(true);
    closeButton.setDefaultButton(true);
    closeButton.setOnAction(_ -> stage.close());

    final HBox buttons = new HBox(10, closeButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    mainContainer.getChildren().addAll(grid, buttons);

    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());
    return new Scene(root, 650, 500);
  }
}
