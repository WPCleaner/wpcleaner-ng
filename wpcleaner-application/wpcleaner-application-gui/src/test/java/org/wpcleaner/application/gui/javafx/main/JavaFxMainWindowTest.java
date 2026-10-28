package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;

@GuiSpringBootTest
class JavaFxMainWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @Autowired private JavaFxMainWindowFactory mainWindowFactory;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private DesktopService desktopService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @BeforeEach
  void setUp() {
    guiLogin.login(
        WIKI, "TestUser", GuiLogin.USER_NAMESPACES, List.of("sysop"), List.of("read", "edit"));
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("JavaFxMainWindow initializes and F1 key displays its help page")
  @Test
  void mainWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    mainWindowFactory.displayMainWindow();
    final JavaFxMainWindow mainWindow = waitForWindow(windowsRegistry, JavaFxMainWindow.class);

    runOnJavaFx(
        () -> {
          Assertions.assertThat(mainWindow.getStage().getTitle()).isEqualTo("WPCleaner");
          Assertions.assertThat(mainWindow.getHelpPage()).isEqualTo("Main");
          Assertions.assertThat(mainWindow.getStage().getOnCloseRequest()).isNotNull();
          Assertions.assertThat(
                  lookup(mainWindow, JavaFxMainWindow.FEEDBACKS_ID, ToolBar.class).getItems())
              .contains(lookup(mainWindow, JavaFxMainWindow.USER_INFORMATION_ID, Button.class));

          final KeyEvent f1 =
              new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false, false, false);
          mainWindow.getStage().getScene().getRoot().fireEvent(f1);
        });

    BDDMockito.then(desktopService)
        .should(BDDMockito.timeout(5000))
        .browse(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Main"));
  }

  @DisplayName("User information button displays the logged in user")
  @Test
  void userInformationButtonDisplaysLoggedInUser()
      throws InterruptedException, ExecutionException, TimeoutException {
    mainWindowFactory.displayMainWindow();
    final JavaFxMainWindow mainWindow = waitForWindow(windowsRegistry, JavaFxMainWindow.class);

    runOnJavaFx(
        () -> lookup(mainWindow, JavaFxMainWindow.USER_INFORMATION_ID, Button.class).fire());

    final UserInformationWindow window =
        waitForWindow(windowsRegistry, UserInformationWindow.class);
    runOnJavaFx(
        () -> {
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(mainWindow.getStage());
          Assertions.assertThat(
                  lookup(window, UserInformationWindow.USERNAME_ID, TextField.class).getText())
              .isEqualTo("TestUser");
          Assertions.assertThat(
                  lookup(window, UserInformationWindow.WIKI_ID, TextField.class).getText())
              .isEqualTo(WIKI.toString());
          Assertions.assertThat(
                  lookup(window, UserInformationWindow.GROUPS_ID, TextArea.class).getText())
              .isEqualTo("sysop");
          Assertions.assertThat(
                  lookup(window, UserInformationWindow.RIGHTS_ID, TextArea.class).getText())
              .isEqualTo("edit\nread");
        });
  }
}
