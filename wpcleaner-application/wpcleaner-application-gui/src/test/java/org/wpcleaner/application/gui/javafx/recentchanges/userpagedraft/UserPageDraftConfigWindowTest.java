package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

class UserPageDraftConfigWindowTest extends JavaFxTest {

  @DisplayName("UserPageDraftConfigWindow initializes controls with existing configuration")
  @Test
  void testUserPageDraftConfigWindowInitializationWithConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

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

          final StackPane root = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) root.getChildren().getFirst();
          final TitledPane userPageGroup = (TitledPane) mainContainer.getChildren().get(0);
          final TitledPane moveDraftGroup = (TitledPane) mainContainer.getChildren().get(1);
          final TitledPane userTalkPageGroup = (TitledPane) mainContainer.getChildren().get(2);

          final GridPane userPageGrid = (GridPane) userPageGroup.getContent();
          final TextArea userPageTextArea = (TextArea) userPageGrid.getChildren().get(3);

          final GridPane userTalkPageGrid = (GridPane) userTalkPageGroup.getContent();
          final VBox tableBox = (VBox) userTalkPageGrid.getChildren().get(3);
          final TableView<?> tableView = (TableView<?>) tableBox.getChildren().getFirst();
          final HBox buttonsBox = (HBox) tableBox.getChildren().get(1);
          final Button addButton = (Button) buttonsBox.getChildren().get(0);
          final Button removeButton = (Button) buttonsBox.getChildren().get(1);

          Assertions.assertThat(addButton.getText()).isEmpty();
          Assertions.assertThat(addButton.getTooltip().getText()).isEqualTo("Add");
          Assertions.assertThat(removeButton.getText()).isEmpty();
          Assertions.assertThat(removeButton.getTooltip().getText()).isEqualTo("Remove");

          Mockito.verify(imageLoader).getImageResource(ImageCollection.LIST_ADD, ImageSize.BUTTON);
          Mockito.verify(imageLoader)
              .getImageResource(ImageCollection.LIST_REMOVE, ImageSize.BUTTON);

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
          Assertions.assertThat(GridPane.getVgrow(tableBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(VBox.getVgrow(tableView)).isEqualTo(Priority.ALWAYS);

          window.getStage().close();
          ownerStage.close();
        });
  }

  @DisplayName("UserPageDraftConfigWindow initializes controls with null configuration")
  @Test
  void testUserPageDraftConfigWindowInitializationWithoutConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final Stage ownerStage = new Stage();
          final AtomicReference<UserPageDraftConfig> validated = new AtomicReference<>();
          final UserPageDraftConfigWindow window =
              new UserPageDraftConfigWindow(services, ownerStage, null, validated::set);

          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          window.getStage().close();
          ownerStage.close();
        });
  }
}
