package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;

@GuiSpringBootTest
class UserInformationWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @Autowired private JavaFxMainWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;

  @BeforeEach
  void setUp() {
    guiLogin.login(
        WIKI,
        "TestUser",
        GuiLogin.USER_NAMESPACES,
        List.of("sysop", "bureaucrat"),
        List.of("read", "edit", "upload"));
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    currentUserService.logout();
  }

  @DisplayName("UserInformationWindow initializes and displays user information correctly")
  @Test
  void windowInitialization() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final UserInformationWindow window =
              new UserInformationWindow(services, null, currentUserService.getCurrentUser());

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("User information");
          Assertions.assertThat(window.getName()).isEqualTo("userInformation");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().getScene().getWidth()).isEqualTo(650);
          Assertions.assertThat(window.getStage().getScene().getHeight()).isEqualTo(500);
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          final Parent root = window.getStage().getScene().getRoot();
          Assertions.assertThat(root).isInstanceOf(StackPane.class);

          final GridPane grid = lookup(window, UserInformationWindow.FIELDS_ID, GridPane.class);
          final Button closeButton = lookup(window, UserInformationWindow.CLOSE_ID, Button.class);

          Assertions.assertThat(grid.getColumnConstraints()).hasSize(2);
          Assertions.assertThat(grid.getColumnConstraints().getFirst().getHgrow())
              .isEqualTo(Priority.NEVER);
          Assertions.assertThat(grid.getColumnConstraints().get(1).getHgrow())
              .isEqualTo(Priority.ALWAYS);

          Assertions.assertThat(closeButton.getText()).isEqualTo("Close");
          Assertions.assertThat(closeButton.isCancelButton()).isTrue();
          Assertions.assertThat(closeButton.isDefaultButton()).isTrue();

          final TextField usernameField =
              lookup(window, UserInformationWindow.USERNAME_ID, TextField.class);
          Assertions.assertThat(usernameField.getText()).isEqualTo("TestUser");
          Assertions.assertThat(usernameField.isEditable()).isFalse();

          final TextField wikiField =
              lookup(window, UserInformationWindow.WIKI_ID, TextField.class);
          Assertions.assertThat(wikiField.getText()).isEqualTo(WIKI.toString());
          Assertions.assertThat(wikiField.isEditable()).isFalse();

          final TextArea groupsField =
              lookup(window, UserInformationWindow.GROUPS_ID, TextArea.class);
          Assertions.assertThat(groupsField.getText()).isEqualTo("bureaucrat\nsysop");
          Assertions.assertThat(groupsField.isEditable()).isFalse();

          final TextArea rightsField =
              lookup(window, UserInformationWindow.RIGHTS_ID, TextArea.class);
          Assertions.assertThat(rightsField.getText()).isEqualTo("edit\nread\nupload");
          Assertions.assertThat(rightsField.isEditable()).isFalse();
        });
  }

  @DisplayName("UserInformationWindow is modal relative to owner window")
  @Test
  void windowModalRelativelyToMainWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final Stage ownerStage = new Stage();
          final UserInformationWindow window =
              new UserInformationWindow(services, ownerStage, currentUserService.getCurrentUser());

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }
}
