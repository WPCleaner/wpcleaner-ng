package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

class UserInformationWindowTest {

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

  @DisplayName("UserInformationWindow initializes and displays user information correctly")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxMainWindowServices services = Mockito.mock(JavaFxMainWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final ConnectedUser user = Mockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);
          Mockito.when(wiki.toString()).thenReturn("Wikipedia (en)");
          Mockito.when(user.username()).thenReturn("TestUser");
          Mockito.when(user.wiki()).thenReturn(wiki);
          Mockito.when(user.groups()).thenReturn(List.of("sysop", "bureaucrat"));
          Mockito.when(user.rights()).thenReturn(List.of("read", "edit", "upload"));

          final UserInformationWindow window = new UserInformationWindow(services, user);

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("User information");
          Assertions.assertThat(window.getName()).isEqualTo("userInformation");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().getScene().getWidth()).isEqualTo(650);
          Assertions.assertThat(window.getStage().getScene().getHeight()).isEqualTo(500);
          Assertions.assertThat(window.getStage().isShowing()).isTrue();

          final Parent root = window.getStage().getScene().getRoot();
          Assertions.assertThat(root).isInstanceOf(StackPane.class);

          final StackPane stackPane = (StackPane) root;
          final VBox mainContainer = (VBox) stackPane.getChildren().getFirst();
          final GridPane grid = (GridPane) mainContainer.getChildren().getFirst();
          final HBox buttons = (HBox) mainContainer.getChildren().get(1);
          final Button closeButton = (Button) buttons.getChildren().getFirst();

          Assertions.assertThat(grid.getColumnConstraints()).hasSize(2);
          Assertions.assertThat(grid.getColumnConstraints().getFirst().getHgrow())
              .isEqualTo(Priority.NEVER);
          Assertions.assertThat(grid.getColumnConstraints().get(1).getHgrow())
              .isEqualTo(Priority.ALWAYS);

          Assertions.assertThat(closeButton.getText()).isEqualTo("Close");
          Assertions.assertThat(closeButton.isCancelButton()).isTrue();
          Assertions.assertThat(closeButton.isDefaultButton()).isTrue();

          final TextField usernameField = (TextField) grid.getChildren().get(1);
          Assertions.assertThat(usernameField.getText()).isEqualTo("TestUser");
          Assertions.assertThat(usernameField.isEditable()).isFalse();

          final TextField wikiField = (TextField) grid.getChildren().get(3);
          Assertions.assertThat(wikiField.getText()).isEqualTo("Wikipedia (en)");
          Assertions.assertThat(wikiField.isEditable()).isFalse();

          final TextArea groupsField = (TextArea) grid.getChildren().get(5);
          Assertions.assertThat(groupsField.getText()).isEqualTo("bureaucrat\nsysop");
          Assertions.assertThat(groupsField.isEditable()).isFalse();

          final TextArea rightsField = (TextArea) grid.getChildren().get(7);
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
          final JavaFxMainWindowServices services = Mockito.mock(JavaFxMainWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);

          final ConnectedUser user = Mockito.mock(ConnectedUser.class);
          final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);
          Mockito.when(wiki.toString()).thenReturn("Wikipedia (en)");
          Mockito.when(user.username()).thenReturn("TestUser");
          Mockito.when(user.wiki()).thenReturn(wiki);
          Mockito.when(user.groups()).thenReturn(List.of());
          Mockito.when(user.rights()).thenReturn(List.of());

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
