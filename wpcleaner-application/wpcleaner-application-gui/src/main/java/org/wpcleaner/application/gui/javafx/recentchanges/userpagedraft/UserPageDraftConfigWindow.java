package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

final class UserPageDraftConfigWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  static final String MOVE_TO_DRAFT_ID = "move-to-draft";
  static final String USER_PAGE_ID = "user-page";
  static final String USER_TALK_PAGE_ID = "user-talk-page";

  private final MoveToDraftConfigurationPane moveDraftGroup;
  private final Consumer<UserPageDraftConfig> onConfigValidated;
  private final ContentReplaceConfigurationPane userPageGroup;
  private final ContentAppendConfigurationPane userTalkPageGroup;

  public UserPageDraftConfigWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      @Nullable final UserPageDraftConfig initialConfig,
      final Consumer<UserPageDraftConfig> onConfigValidated) {
    super(services, Objects.requireNonNull(owner));
    stage.initModality(Modality.WINDOW_MODAL);
    this.onConfigValidated = Objects.requireNonNull(onConfigValidated);
    stage.setTitle(GT._T("Configure User Page Draft"));

    this.userPageGroup =
        new ContentReplaceConfigurationPane(
            GT._T("User page"), initialConfig != null ? initialConfig.userPage() : null);

    this.moveDraftGroup =
        new MoveToDraftConfigurationPane(
            GT._T("Move draft"), initialConfig != null ? initialConfig.moveToDraft() : null);

    this.userTalkPageGroup =
        new ContentAppendConfigurationPane(
            GT._T("User talk page"),
            services.imageLoader(),
            initialConfig != null ? initialConfig.userTalkPage() : null);
    this.userPageGroup.setId(USER_PAGE_ID);
    this.moveDraftGroup.setId(MOVE_TO_DRAFT_ID);
    this.userTalkPageGroup.setId(USER_TALK_PAGE_ID);

    initialize();
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "User page draft";
  }

  @Override
  public String getName() {
    return "userPageDraftConfig";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setPrefWidth(750);

    VBox.setVgrow(userPageGroup, Priority.ALWAYS);
    VBox.setVgrow(moveDraftGroup, Priority.ALWAYS);
    VBox.setVgrow(userTalkPageGroup, Priority.ALWAYS);

    mainContainer.getChildren().add(userPageGroup);
    mainContainer.getChildren().add(moveDraftGroup);
    mainContainer.getChildren().add(userTalkPageGroup);

    final Button okButton = new Button(GT._T("OK"));
    okButton.setDefaultButton(true);
    okButton.setOnAction(_ -> handleOk());

    final Button cancelButton = new Button(GT._T("Cancel"));
    cancelButton.setCancelButton(true);
    cancelButton.setOnAction(_ -> stage.close());

    final HBox buttons = new HBox(10, okButton, cancelButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    mainContainer.getChildren().add(buttons);
    mainContainer.disableProperty().bind(loading);

    root.getChildren().addAll(mainContainer, progressOverlay);
    return new Scene(root);
  }

  private void handleOk() {
    if (!userPageGroup.isComplete()
        || !moveDraftGroup.isComplete()
        || !userTalkPageGroup.isComplete()) {
      showError(GT._T("Please fill in all mandatory fields."));
      return;
    }
    final UserPageDraftConfig config = convertResult();
    onConfigValidated.accept(config);
    stage.close();
  }

  private UserPageDraftConfig convertResult() {
    return new UserPageDraftConfig(
        moveDraftGroup.getMoveToDraftConfiguration(),
        userPageGroup.getContentReplaceConfiguration(),
        userTalkPageGroup.getContentAppendConfiguration());
  }
}
