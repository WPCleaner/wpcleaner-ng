package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

class WikiDefinitionWindowTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runOnJavaFx(final Runnable runnable)
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (final AssertionError | Exception e) {
            future.completeExceptionally(e);
          }
        });
    future.get(5, TimeUnit.SECONDS);
  }

  @DisplayName("WikiDefinitionWindow initializes correctly")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services = Mockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = Mockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = Mockito.mock(KnownDefinitions.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.apiSiteInfo()).thenReturn(apiSiteInfo);
          Mockito.when(services.knownDefinitions()).thenReturn(knownDefinitions);

          final AtomicBoolean added = new AtomicBoolean(false);
          final WikiDefinitionWindow window =
              new WikiDefinitionWindow(services, _ -> added.set(true));

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("Add wiki");
          Assertions.assertThat(window.getName()).isEqualTo("wikiDefinition");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          final Parent root = window.getStage().getScene().getRoot();
          Assertions.assertThat(root).isInstanceOf(StackPane.class);

          final StackPane stackPane = (StackPane) root;
          final VBox mainContainer = (VBox) stackPane.getChildren().getFirst();
          final HBox buttons = (HBox) mainContainer.getChildren().get(1);
          final Button cancelButton = (Button) buttons.getChildren().get(1);

          Assertions.assertThat(cancelButton.getText()).isEqualTo("Cancel");
          Assertions.assertThat(added).isFalse();
        });
  }

  @DisplayName("WikiDefinitionWindow is modal relative to JavaFxLoginWindow")
  @Test
  void testWindowModalRelativelyToLoginWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services = Mockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = Mockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = Mockito.mock(KnownDefinitions.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.apiSiteInfo()).thenReturn(apiSiteInfo);
          Mockito.when(services.knownDefinitions()).thenReturn(knownDefinitions);

          final Stage ownerStage = new Stage();
          final WikiDefinitionWindow window = new WikiDefinitionWindow(services, ownerStage);

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);
        });
  }
}
