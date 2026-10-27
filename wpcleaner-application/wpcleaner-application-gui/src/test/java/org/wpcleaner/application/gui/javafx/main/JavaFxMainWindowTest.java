package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxSaveWindowsPositionAction;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowFactory;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettings;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxMainWindowTest extends JavaFxTest {

  @DisplayName("JavaFxMainWindow initializes and customizes feedbacks toolbar with user button")
  @Test
  void testMainWindowInitializationAndUserButton()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxMainWindowServices services = BDDMockito.mock(JavaFxMainWindowServices.class);
          final ApiRandom apiRandom = BDDMockito.mock(ApiRandom.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final InterestingSettingsManager interestingSettings =
              BDDMockito.mock(InterestingSettingsManager.class);
          final JavaFxRecentChangesWindowFactory recentChangesWindowFactory =
              BDDMockito.mock(JavaFxRecentChangesWindowFactory.class);
          final UrlService urlService = BDDMockito.mock(UrlService.class);
          final CurrentUserService currentUserService = BDDMockito.mock(CurrentUserService.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          final ConnectedUser user = BDDMockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);

          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.apiRandom()).willReturn(apiRandom);
          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.interestingSettings()).willReturn(interestingSettings);
          BDDMockito.given(services.recentChangesWindowFactory())
              .willReturn(recentChangesWindowFactory);
          BDDMockito.given(services.urlService()).willReturn(urlService);
          BDDMockito.given(services.user()).willReturn(currentUserService);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

          final InterestingSettings interestingSettingsObj =
              BDDMockito.mock(InterestingSettings.class);
          BDDMockito.given(interestingSettings.getCurrentSettings())
              .willReturn(interestingSettingsObj);
          BDDMockito.given(interestingSettingsObj.getByWikiSettings(BDDMockito.any()))
              .willReturn(Optional.empty());

          BDDMockito.given(currentUserService.getCurrentUser()).willReturn(user);
          BDDMockito.given(user.username()).willReturn("TestUser");
          BDDMockito.given(user.wiki()).willReturn(wiki);
          BDDMockito.given(user.groups()).willReturn(List.of("sysop"));
          BDDMockito.given(user.rights()).willReturn(List.of("read", "edit"));
          BDDMockito.given(user.demo()).willReturn(false);

          final JavaFxSaveWindowsPositionAction saveAction =
              BDDMockito.mock(JavaFxSaveWindowsPositionAction.class);
          BDDMockito.given(actionServices.saveWindowsPosition()).willReturn(saveAction);

          final JavaFxMainWindow mainWindow = new JavaFxMainWindow(services);

          Assertions.assertThat(mainWindow.getStage().getTitle()).isEqualTo("WPCleaner");
          Assertions.assertThat(mainWindow.getHelpPage()).isEqualTo("Main");
          Assertions.assertThat(mainWindow.getStage().getOnCloseRequest()).isNotNull();

          final KeyEvent f1 =
              new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false, false, false);
          mainWindow.getStage().getScene().getRoot().fireEvent(f1);
          BDDMockito.then(actionServices).should().displayHelp(mainWindow);

          final javafx.scene.Parent rootPane = mainWindow.getStage().getScene().getRoot();
          final VBox mainContainer = (VBox) rootPane.getChildrenUnmodifiable().getFirst();
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
          mainWindow.getStage().close();
        });
  }
}
