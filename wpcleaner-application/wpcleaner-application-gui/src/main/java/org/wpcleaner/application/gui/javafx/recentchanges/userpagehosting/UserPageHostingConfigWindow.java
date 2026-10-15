package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

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
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

final class UserPageHostingConfigWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private final Consumer<UserPageHostingConfig> onConfigValidated;
  private final ContentReplaceConfigurationPane userPageGroup;
  private final ContentAppendConfigurationPane userTalkPageGroup;

  public UserPageHostingConfigWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      @Nullable final UserPageHostingConfig initialConfig,
      final Consumer<UserPageHostingConfig> onConfigValidated) {
    super(services, Objects.requireNonNull(owner));
    stage.initModality(Modality.WINDOW_MODAL);
    this.onConfigValidated = Objects.requireNonNull(onConfigValidated);
    stage.setTitle(GT._T("Configure User Page Hosting Prevention"));

    this.userPageGroup =
        new ContentReplaceConfigurationPane(
            GT._T("User page"), initialConfig != null ? initialConfig.userPage() : null);

    this.userTalkPageGroup =
        new ContentAppendConfigurationPane(
            GT._T("User talk page"),
            services.imageLoader(),
            initialConfig != null ? initialConfig.userTalkPage() : null);

    initialize();
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "User page hosting";
  }

  @Override
  public String getName() {
    return "userPageHostingConfig";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setPrefWidth(750);

    VBox.setVgrow(userPageGroup, Priority.ALWAYS);
    VBox.setVgrow(userTalkPageGroup, Priority.ALWAYS);

    mainContainer.getChildren().add(userPageGroup);
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
    if (!userPageGroup.isComplete() || !userTalkPageGroup.isComplete()) {
      showError(GT._T("Please fill in all mandatory fields."));
      return;
    }
    final UserPageHostingConfig config = convertResult();
    onConfigValidated.accept(config);
    stage.close();
  }

  private UserPageHostingConfig convertResult() {
    return new UserPageHostingConfig(
        userPageGroup.getContentReplaceConfiguration(),
        userTalkPageGroup.getContentAppendConfiguration());
  }
}
