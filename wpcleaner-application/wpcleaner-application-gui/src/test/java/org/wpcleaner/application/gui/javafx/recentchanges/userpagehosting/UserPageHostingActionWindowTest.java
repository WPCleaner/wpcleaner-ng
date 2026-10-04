package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Label;
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
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

class UserPageHostingActionWindowTest extends JavaFxTest {

  @DisplayName("UserPageHostingActionWindow initializes controls properly")
  @Test
  void testUserPageHostingActionWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxRecentChangesWindowServices services =
              Mockito.mock(JavaFxRecentChangesWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.colorizer()).thenReturn(colorizer);
          Mockito.when(services.pageAnalysisFactory()).thenReturn(pageAnalysisFactory);

          final Stage ownerStage = new Stage();
          final UserPageHostingConfig config =
              new UserPageHostingConfig(
                  "userPageSummary",
                  "userPageText",
                  "userTalkPageSummary",
                  List.of(new UserTalkPageTextConfig("label1", "text1", true)));

          final AtomicReference<UserPageHostingActionParams> confirmed = new AtomicReference<>();
          final UserPageHostingActionWindow window =
              new UserPageHostingActionWindow(
                  services,
                  ownerStage,
                  config,
                  "User:Test",
                  "Initial user page",
                  "User talk:Test",
                  "Initial talk page",
                  confirmed::set);

          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Confirm User Page Hosting Action");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingAction");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final StackPane root = (StackPane) window.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) root.getChildren().getFirst();
          Assertions.assertThat(mainContainer.getPrefWidth()).isEqualTo(1900.0);

          final HBox previewsBox = (HBox) mainContainer.getChildren().get(2);
          Assertions.assertThat(previewsBox.getChildren()).hasSize(2);

          final VBox userPageBox = (VBox) previewsBox.getChildren().getFirst();
          final Label userPageLabel = (Label) userPageBox.getChildren().getFirst();
          Assertions.assertThat(userPageLabel.getText()).isEqualTo("User page");
          final PageAnalysisScrollPane userPageScrollPane =
              (PageAnalysisScrollPane) userPageBox.getChildren().get(1);
          Assertions.assertThat(userPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);

          final VBox userTalkPageBox = (VBox) previewsBox.getChildren().get(1);
          final Label userTalkPageLabel = (Label) userTalkPageBox.getChildren().getFirst();
          Assertions.assertThat(userTalkPageLabel.getText()).isEqualTo("User talk page");
          final PageAnalysisScrollPane userTalkPageScrollPane =
              (PageAnalysisScrollPane) userTalkPageBox.getChildren().get(1);
          Assertions.assertThat(userTalkPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);

          Assertions.assertThat(userPageBox.getPrefWidth())
              .isEqualTo(userTalkPageBox.getPrefWidth());
          Assertions.assertThat(HBox.getHgrow(userPageBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(HBox.getHgrow(userTalkPageBox)).isEqualTo(Priority.ALWAYS);

          window.getStage().close();
          ownerStage.close();
        });
  }
}
