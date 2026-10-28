package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.login.Login;
import org.wpcleaner.api.api.login.MockApiLogin;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.main.JavaFxMainWindow;

@GuiSpringBootTest
class JavaFxLoginWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String USERNAME = "Jane Doe";

  @Autowired private JavaFxLoginWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private DesktopService desktopService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private MockApiLogin mockApiLogin;
  @Autowired private MockApiTokens mockApiTokens;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("JavaFxLoginWindow initializes correctly and F1 key displays its help page")
  @Test
  void windowInitialization() throws InterruptedException, ExecutionException, TimeoutException {
    final JavaFxLoginWindow window = createWindow();

    runOnJavaFx(
        () -> {
          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("WPCleaner");
          Assertions.assertThat(window.getName()).isEqualTo("login");
          Assertions.assertThat(window.getHelpPage()).isEqualTo("Login");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().getOnCloseRequest()).isNotNull();

          final KeyEvent f1 =
              new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false, false, false);
          window.getStage().getScene().getRoot().fireEvent(f1);
        });

    BDDMockito.then(desktopService)
        .should(BDDMockito.timeout(5000))
        .browse(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"));
  }

  @DisplayName("Login logs the user in and opens the main window")
  @Test
  void loginOpensMainWindow() throws InterruptedException, ExecutionException, TimeoutException {
    guiLogin.givenLogin(WIKI, USERNAME, GuiLogin.USER_NAMESPACES);
    final JavaFxLoginWindow window = createWindow();

    runOnJavaFx(
        () -> {
          fillForm(window, GuiLogin.PASSWORD);
          lookup(window, JavaFxLoginWindow.LOGIN_ID, Button.class).fire();
        });

    Assertions.assertThat(waitForWindow(windowsRegistry, JavaFxMainWindow.class)).isNotNull();
    waitForJavaFx(() -> Optional.of(window.getStage().isShowing()).filter(showing -> !showing));
    final ConnectedUser user = currentUserService.getCurrentUser();
    Assertions.assertThat(user.isLoggedIn()).isTrue();
    Assertions.assertThat(user.demo()).isFalse();
    Assertions.assertThat(user.wiki()).isEqualTo(WIKI);
    Assertions.assertThat(user.username()).isEqualTo(USERNAME);
  }

  @DisplayName("Login shows an error and keeps the login window open when login fails")
  @Test
  void loginFailureShowsError() throws InterruptedException, ExecutionException, TimeoutException {
    mockApiTokens.givenLoginToken(WIKI, "login-token");
    mockApiLogin
        .givenLogin(WIKI, USERNAME, "wrong password", "login-token")
        .willReturn(new Login(Login.RESULT_FAILED, 0L, USERNAME));
    final JavaFxLoginWindow window = createWindow();

    runOnJavaFx(
        () -> {
          fillForm(window, "wrong password");
          lookup(window, JavaFxLoginWindow.LOGIN_ID, Button.class).fire();
        });

    Assertions.assertThat(closeAlert(window)).isEqualTo("Login failed");
    Assertions.assertThat(callOnJavaFx(() -> window.getStage().isShowing())).isTrue();
    Assertions.assertThat(currentUserService.getCurrentUser().isLoggedIn()).isFalse();
  }

  @DisplayName("Demo starts the demo mode without login and opens the main window")
  @Test
  void demoOpensMainWindow() throws InterruptedException, ExecutionException, TimeoutException {
    guiLogin.givenDemoLogin(WIKI, USERNAME, GuiLogin.USER_NAMESPACES);
    final JavaFxLoginWindow window = createWindow();

    runOnJavaFx(
        () -> {
          fillForm(window, "");
          lookup(window, JavaFxLoginWindow.DEMO_ID, Button.class).fire();
        });

    Assertions.assertThat(waitForWindow(windowsRegistry, JavaFxMainWindow.class)).isNotNull();
    final ConnectedUser user = currentUserService.getCurrentUser();
    Assertions.assertThat(user.isLoggedIn()).isTrue();
    Assertions.assertThat(user.demo()).isTrue();
    Assertions.assertThat(user.username()).isEqualTo(USERNAME);
  }

  private JavaFxLoginWindow createWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(() -> new JavaFxLoginWindow(services));
  }

  @SuppressWarnings("unchecked")
  private static void fillForm(final JavaFxLoginWindow window, final String password) {
    lookup(window, JavaFxLoginWindow.WIKI_ID, ComboBox.class).getSelectionModel().select(WIKI);
    lookup(window, JavaFxLoginWindow.USER_ID, ComboBox.class).getSelectionModel().select(USERNAME);
    lookup(window, JavaFxLoginWindow.PASSWORD_ID, PasswordField.class).setText(password);
  }
}
