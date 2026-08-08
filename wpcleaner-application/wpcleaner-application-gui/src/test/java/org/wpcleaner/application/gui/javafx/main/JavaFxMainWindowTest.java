package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxSaveWindowsPositionAction;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowFactory;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxMainWindowTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runOnJavaFx(final Runnable runnable)
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
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

  @DisplayName("JavaFxMainWindow initializes and customizes feedbacks toolbar with user button")
  @Test
  void testMainWindowInitializationAndUserButton()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxMainWindowServices services = Mockito.mock(JavaFxMainWindowServices.class);
          final ApiRandom apiRandom = Mockito.mock(ApiRandom.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final InterestingSettingsManager interestingSettings =
              Mockito.mock(InterestingSettingsManager.class);
          final JavaFxRecentChangesWindowFactory recentChangesWindowFactory =
              Mockito.mock(JavaFxRecentChangesWindowFactory.class);
          final UrlService urlService = Mockito.mock(UrlService.class);
          final CurrentUserService currentUserService = Mockito.mock(CurrentUserService.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          final ConnectedUser user = Mockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);

          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.apiRandom()).thenReturn(apiRandom);
          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.interestingSettings()).thenReturn(interestingSettings);
          Mockito.when(services.recentChangesWindowFactory())
              .thenReturn(recentChangesWindowFactory);
          Mockito.when(services.urlService()).thenReturn(urlService);
          Mockito.when(services.user()).thenReturn(currentUserService);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final InterestingSettings interestingSettingsObj =
              Mockito.mock(InterestingSettings.class);
          Mockito.when(interestingSettings.getCurrentSettings()).thenReturn(interestingSettingsObj);
          Mockito.when(interestingSettingsObj.getByWikiSettings(Mockito.any()))
              .thenReturn(Optional.empty());

          Mockito.when(currentUserService.getCurrentUser()).thenReturn(user);
          Mockito.when(user.username()).thenReturn("TestUser");
          Mockito.when(user.wiki()).thenReturn(wiki);
          Mockito.when(user.groups()).thenReturn(List.of("sysop"));
          Mockito.when(user.rights()).thenReturn(List.of("read", "edit"));
          Mockito.when(user.demo()).thenReturn(false);

          final JavaFxSaveWindowsPositionAction saveAction =
              Mockito.mock(JavaFxSaveWindowsPositionAction.class);
          Mockito.when(actionServices.saveWindowsPosition()).thenReturn(saveAction);

          final JavaFxMainWindow mainWindow = new JavaFxMainWindow(services);

          Assertions.assertThat(mainWindow.getTitle()).isEqualTo("WPCleaner");

          final javafx.scene.Parent rootPane = mainWindow.getScene().getRoot();
          final VBox mainContainer = (VBox) rootPane.getChildrenUnmodifiable().get(0);
          final ToolBar feedbacksToolBar = (ToolBar) mainContainer.getChildren().get(2);
          final List<Button> buttons =
              feedbacksToolBar.getItems().stream()
                  .filter(Button.class::isInstance)
                  .map(Button.class::cast)
                  .toList();

          final Optional<Button> userButton =
              buttons.stream()
                  .filter(
                      btn ->
                          btn.getTooltip() != null
                              && "User information".equals(btn.getTooltip().getText()))
                  .findFirst();

          Assertions.assertThat(userButton).isPresent();
        });
  }
}
