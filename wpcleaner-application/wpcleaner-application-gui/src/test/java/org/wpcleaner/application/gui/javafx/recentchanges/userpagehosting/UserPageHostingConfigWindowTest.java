package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationPane;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfigurationPane;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

class UserPageHostingConfigWindowTest extends JavaFxTest {

  @DisplayName("UserPageHostingConfigWindow initializes controls with existing configuration")
  @Test
  void testUserPageHostingConfigWindowInitializationWithConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

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

          BDDMockito.then(imageLoader)
              .should()
              .getImageResource(ImageCollection.LIST_ADD, ImageSize.BUTTON);
          BDDMockito.then(imageLoader)
              .should()
              .getImageResource(ImageCollection.LIST_REMOVE, ImageSize.BUTTON);

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
  void testUserPageHostingConfigWindowInitializationWithoutConfig()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

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
  void testUserPageHostingConfigWindowVerticalResize()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              BDDMockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

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
}
