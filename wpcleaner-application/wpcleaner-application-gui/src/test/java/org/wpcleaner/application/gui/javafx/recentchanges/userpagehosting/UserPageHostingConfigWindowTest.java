package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfigurationPane;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class UserPageHostingConfigWindowTest extends JavaFxTest {

  @Autowired private JavaFxRecentChangesWindowServices services;

  @DisplayName("UserPageHostingConfigWindow initializes controls with existing configuration")
  @Test
  void userPageHostingConfigWindowInitializationWithConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final UserPageHostingConfig initialConfig =
              new UserPageHostingConfig(
                  new ContentReplaceConfiguration("userPageSummary", "userPageText"),
                  new ContentAppendConfiguration(
                      "userTalkPageSummary",
                      List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

          final AtomicReference<UserPageHostingConfig> validated = new AtomicReference<>();
          final UserPageHostingConfigWindow window =
              new UserPageHostingConfigWindow(services, ownerStage, initialConfig, validated::set);

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Configure User Page Hosting Prevention");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingConfig");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final TitledPane userPageGroup =
              lookup(window, UserPageHostingConfigWindow.USER_PAGE_ID, TitledPane.class);
          final TitledPane userTalkPageGroup =
              lookup(window, UserPageHostingConfigWindow.USER_TALK_PAGE_ID, TitledPane.class);

          final TextArea userPageTextArea =
              lookup(userPageGroup, ContentReplaceConfigurationPane.TEXT_ID, TextArea.class);

          final TableView<?> tableView =
              lookup(userTalkPageGroup, ContentAppendConfigurationPane.TEXTS_ID, TableView.class);
          final Button addButton =
              lookup(userTalkPageGroup, ContentAppendConfigurationPane.ADD_ID, Button.class);
          final Button removeButton =
              lookup(userTalkPageGroup, ContentAppendConfigurationPane.REMOVE_ID, Button.class);

          Assertions.assertThat(addButton.getText()).isEmpty();
          Assertions.assertThat(addButton.getTooltip().getText()).isEqualTo("Add");
          Assertions.assertThat(removeButton.getText()).isEmpty();
          Assertions.assertThat(removeButton.getTooltip().getText()).isEqualTo("Remove");

          Assertions.assertThat(addButton.getGraphic()).isInstanceOf(ImageView.class);
          Assertions.assertThat(removeButton.getGraphic()).isInstanceOf(ImageView.class);

          Assertions.assertThat(removeButton.isDisabled()).isTrue();
          tableView.getSelectionModel().select(0);
          Assertions.assertThat(removeButton.isDisabled()).isFalse();
          tableView.getSelectionModel().clearSelection();
          Assertions.assertThat(removeButton.isDisabled()).isTrue();

          Assertions.assertThat(VBox.getVgrow(userPageGroup)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(userTalkPageGroup)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(GridPane.getVgrow(userPageTextArea)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getVgrow(tableView.getParent()))
              .isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(tableView)).isEqualTo(Priority.ALWAYS);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("UserPageHostingConfigWindow initializes controls with null configuration")
  @Test
  void userPageHostingConfigWindowInitializationWithoutConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final UserPageHostingConfigWindow window =
              new UserPageHostingConfigWindow(services, ownerStage, null, _ -> {});

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Configure User Page Hosting Prevention");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingConfig");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("UserPageHostingConfigWindow expands components vertically when resized")
  @Test
  void userPageHostingConfigWindowVerticalResize()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final UserPageHostingConfigWindow window =
              new UserPageHostingConfigWindow(services, ownerStage, null, _ -> {});

          final StackPane root = (StackPane) window.getStage().getScene().getRoot();
          root.applyCss();
          root.layout();

          final TitledPane userPageGroup =
              lookup(window, UserPageHostingConfigWindow.USER_PAGE_ID, TitledPane.class);
          final TitledPane userTalkPageGroup =
              lookup(window, UserPageHostingConfigWindow.USER_TALK_PAGE_ID, TitledPane.class);

          final TextArea userPageTextArea =
              lookup(userPageGroup, ContentReplaceConfigurationPane.TEXT_ID, TextArea.class);

          final TableView<?> tableView =
              lookup(userTalkPageGroup, ContentAppendConfigurationPane.TEXTS_ID, TableView.class);

          final double initialUserPageHeight = userPageGroup.getHeight();
          final double initialUserTalkPageHeight = userTalkPageGroup.getHeight();
          final double initialTextAreaHeight = userPageTextArea.getHeight();
          final double initialTableViewHeight = tableView.getHeight();

          root.resize(root.getWidth(), root.getHeight() + 200.0);
          root.applyCss();
          root.layout();

          Assertions.assertThat(userPageGroup.getHeight()).isGreaterThan(initialUserPageHeight);
          Assertions.assertThat(userTalkPageGroup.getHeight())
              .isGreaterThan(initialUserTalkPageHeight);
          Assertions.assertThat(userPageTextArea.getHeight()).isGreaterThan(initialTextAreaHeight);
          Assertions.assertThat(tableView.getHeight()).isGreaterThan(initialTableViewHeight);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("OK passes the edited configuration and closes the window")
  @Test
  void okPassesEditedConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageHostingConfig initialConfig =
        new UserPageHostingConfig(
            new ContentReplaceConfiguration("userPageSummary", "userPageText"),
            new ContentAppendConfiguration(
                "userTalkPageSummary",
                List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));
    final AtomicReference<UserPageHostingConfig> validated = new AtomicReference<>();
    final Stage ownerStage = callOnJavaFx(Stage::new);
    final UserPageHostingConfigWindow window =
        callOnJavaFx(
            () ->
                new UserPageHostingConfigWindow(
                    services, ownerStage, initialConfig, validated::set));

    runOnJavaFx(
        () -> {
          final TitledPane userPageGroup =
              lookup(window, UserPageHostingConfigWindow.USER_PAGE_ID, TitledPane.class);
          lookup(userPageGroup, ContentReplaceConfigurationPane.SUMMARY_ID, TextField.class)
              .setText("newSummary");
          lookup(window, UserPageHostingConfigWindow.OK_ID, Button.class).fire();
          Assertions.assertThat(window.getStage().isShowing()).isFalse();
          ownerStage.close();
        });

    Assertions.assertThat(validated.get())
        .isEqualTo(
            new UserPageHostingConfig(
                new ContentReplaceConfiguration("newSummary", "userPageText"),
                initialConfig.userTalkPage()));
  }

  @DisplayName("OK shows an error and keeps the window open when a mandatory field is missing")
  @Test
  void okWithMissingFieldShowsError()
      throws InterruptedException, ExecutionException, TimeoutException {
    final AtomicReference<UserPageHostingConfig> validated = new AtomicReference<>();
    final Stage ownerStage = callOnJavaFx(Stage::new);
    final UserPageHostingConfigWindow window =
        callOnJavaFx(
            () -> new UserPageHostingConfigWindow(services, ownerStage, null, validated::set));

    Platform.runLater(() -> lookup(window, UserPageHostingConfigWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window)).isEqualTo("Please fill in all mandatory fields.");
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().isShowing())).isTrue();
    Assertions.assertThat(validated.get()).isNull();
    runOnJavaFx(
        () -> {
          window.getStage().close();
          ownerStage.close();
        });
  }
}
