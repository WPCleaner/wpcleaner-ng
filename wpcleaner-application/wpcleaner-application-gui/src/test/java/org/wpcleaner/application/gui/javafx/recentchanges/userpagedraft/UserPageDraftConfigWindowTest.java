package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

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
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfigurationPane;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class UserPageDraftConfigWindowTest extends JavaFxTest {

  @Autowired private JavaFxRecentChangesWindowServices services;

  @DisplayName("UserPageDraftConfigWindow initializes controls with existing configuration")
  @Test
  void userPageDraftConfigWindowInitializationWithConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final UserPageDraftConfig initialConfig =
              new UserPageDraftConfig(
                  new MoveToDraftConfiguration("Draft", "moveSummary"),
                  new ContentReplaceConfiguration("userPageSummary", "userPageText"),
                  new ContentAppendConfiguration(
                      "userTalkPageSummary",
                      List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

          final AtomicReference<UserPageDraftConfig> validated = new AtomicReference<>();
          final UserPageDraftConfigWindow window =
              new UserPageDraftConfigWindow(services, ownerStage, initialConfig, validated::set);

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Configure User Page Draft");
          Assertions.assertThat(window.getName()).isEqualTo("userPageDraftConfig");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final TitledPane userPageGroup =
              lookup(window, UserPageDraftConfigWindow.USER_PAGE_ID, TitledPane.class);
          final TitledPane moveDraftGroup =
              lookup(window, UserPageDraftConfigWindow.MOVE_TO_DRAFT_ID, TitledPane.class);
          final TitledPane userTalkPageGroup =
              lookup(window, UserPageDraftConfigWindow.USER_TALK_PAGE_ID, TitledPane.class);

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
          Assertions.assertThat(VBox.getVgrow(moveDraftGroup)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(userTalkPageGroup)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(moveDraftGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(GridPane.getVgrow(userPageTextArea)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(GridPane.getVgrow(tableView.getParent()))
              .isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(tableView)).isEqualTo(Priority.ALWAYS);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("UserPageDraftConfigWindow initializes controls with null configuration")
  @Test
  void userPageDraftConfigWindowInitializationWithoutConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final AtomicReference<UserPageDraftConfig> validated = new AtomicReference<>();
          final UserPageDraftConfigWindow window =
              new UserPageDraftConfigWindow(services, ownerStage, null, validated::set);

          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("OK passes the edited configuration and closes the window")
  @Test
  void okPassesEditedConfiguration()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftConfig initialConfig =
        new UserPageDraftConfig(
            new MoveToDraftConfiguration("Draft", "moveSummary"),
            new ContentReplaceConfiguration("userPageSummary", "userPageText"),
            new ContentAppendConfiguration(
                "userTalkPageSummary",
                List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));
    final AtomicReference<UserPageDraftConfig> validated = new AtomicReference<>();
    final Stage ownerStage = callOnJavaFx(Stage::new);
    final UserPageDraftConfigWindow window =
        callOnJavaFx(
            () ->
                new UserPageDraftConfigWindow(services, ownerStage, initialConfig, validated::set));

    runOnJavaFx(
        () -> {
          final TitledPane moveDraftGroup =
              lookup(window, UserPageDraftConfigWindow.MOVE_TO_DRAFT_ID, TitledPane.class);
          lookup(moveDraftGroup, MoveToDraftConfigurationPane.SUB_PAGE_NAME_ID, TextField.class)
              .setText("Brouillon");
          lookup(window, UserPageDraftConfigWindow.OK_ID, Button.class).fire();
          Assertions.assertThat(window.getStage().isShowing()).isFalse();
          ownerStage.close();
        });

    Assertions.assertThat(validated.get())
        .isEqualTo(
            new UserPageDraftConfig(
                new MoveToDraftConfiguration("Brouillon", "moveSummary"),
                initialConfig.userPage(),
                initialConfig.userTalkPage()));
  }

  @DisplayName("OK shows an error and keeps the window open when a mandatory field is missing")
  @Test
  void okWithMissingFieldShowsError()
      throws InterruptedException, ExecutionException, TimeoutException {
    final AtomicReference<UserPageDraftConfig> validated = new AtomicReference<>();
    final Stage ownerStage = callOnJavaFx(Stage::new);
    final UserPageDraftConfigWindow window =
        callOnJavaFx(
            () -> new UserPageDraftConfigWindow(services, ownerStage, null, validated::set));

    Platform.runLater(() -> lookup(window, UserPageDraftConfigWindow.OK_ID, Button.class).fire());

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
