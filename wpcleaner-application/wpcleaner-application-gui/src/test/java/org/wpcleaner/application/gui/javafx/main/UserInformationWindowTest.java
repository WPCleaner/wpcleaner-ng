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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

class UserInformationWindowTest extends JavaFxTest {

  @DisplayName("UserInformationWindow initializes and displays user information correctly")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxMainWindowServices services = BDDMockito.mock(JavaFxMainWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

          final ConnectedUser user = BDDMockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
          BDDMockito.given(wiki.toString()).willReturn("Wikipedia (en)");
          BDDMockito.given(user.username()).willReturn("TestUser");
          BDDMockito.given(user.wiki()).willReturn(wiki);
          BDDMockito.given(user.groups()).willReturn(List.of("sysop", "bureaucrat"));
          BDDMockito.given(user.rights()).willReturn(List.of("read", "edit", "upload"));

          final UserInformationWindow window = new UserInformationWindow(services, null, user);

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
          Assertions.assertThat(wikiField.getText()).isEqualTo("Wikipedia (en)");
          Assertions.assertThat(wikiField.isEditable()).isFalse();

          final TextArea groupsField =
              lookup(window, UserInformationWindow.GROUPS_ID, TextArea.class);
          Assertions.assertThat(groupsField.getText()).isEqualTo("bureaucrat\nsysop");
          Assertions.assertThat(groupsField.isEditable()).isFalse();

          final TextArea rightsField =
              lookup(window, UserInformationWindow.RIGHTS_ID, TextArea.class);
          Assertions.assertThat(rightsField.getText()).isEqualTo("edit\nread\nupload");
          Assertions.assertThat(rightsField.isEditable()).isFalse();

          window.getStage().close();
        });
  }

  @DisplayName("UserInformationWindow is modal relative to owner window")
  @Test
  void testWindowModalRelativelyToMainWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxMainWindowServices services = BDDMockito.mock(JavaFxMainWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);

          final ConnectedUser user = BDDMockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
          BDDMockito.given(wiki.toString()).willReturn("Wikipedia (en)");
          BDDMockito.given(user.username()).willReturn("TestUser");
          BDDMockito.given(user.wiki()).willReturn(wiki);
          BDDMockito.given(user.groups()).willReturn(List.of());
          BDDMockito.given(user.rights()).willReturn(List.of());

          final Stage ownerStage = new Stage();
          final UserInformationWindow window =
              new UserInformationWindow(services, ownerStage, user);

          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          window.getStage().close();
          ownerStage.close();
        });
  }
}
