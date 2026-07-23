/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.gui.javafx.FeedbacksToolBar;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class JavaFxLoginWindow extends Stage {

  private final JavaFxLoginWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final BooleanProperty loading;
  private final JavaFxProgressTracker progressTracker;

  public JavaFxLoginWindow(final JavaFxLoginWindowServices services) {
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.loading = new SimpleBooleanProperty(false);
    this.progressTracker = JavaFxProgressTracker.forObservable(loading);
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner");
    imageLoader.setWindowIcon(this);

    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(6, 15, 6, 15));
    mainContainer.setAlignment(Pos.CENTER);

    final WikiInput wiki =
        new WikiInput(services.knownDefinitions(), imageLoader, services.desktopService());
    final LanguageInput language = new LanguageInput(imageLoader, services.desktopService());
    final UserInput user = new UserInput(imageLoader);
    final PasswordInput password = new PasswordInput(imageLoader, services.desktopService());

    final GridPane grid = createFormGrid(wiki, language, user, password);
    final HBox buttons = createButtonsPanel(wiki, user, password);
    final ToolBar feedbacks = createFeedbacksToolbar();

    mainContainer.getChildren().addAll(grid, buttons, feedbacks);

    grid.disableProperty().bind(loading);
    buttons.disableProperty().bind(loading);

    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());

    final Scene scene = new Scene(root, 650, 240);
    setScene(scene);

    wiki.addSelectionListener(
        (_, _, newVal) -> {
          if (newVal != null) {
            services
                .credentialsProvider()
                .getCredential(newVal)
                .ifPresent(
                    credential -> {
                      user.setUser(credential.username());
                      password.setPassword(credential.password());
                    });
          }
        });

    sizeToScene();
  }

  private GridPane createFormGrid(
      final WikiInput wiki,
      final LanguageInput language,
      final UserInput user,
      final PasswordInput password) {
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);

    final ColumnConstraints colLabel = new ColumnConstraints();
    colLabel.setPercentWidth(20);
    colLabel.setHgrow(Priority.NEVER);

    final ColumnConstraints colIcon = new ColumnConstraints();
    colIcon.setPercentWidth(8);
    colIcon.setHgrow(Priority.NEVER);

    final ColumnConstraints colField = new ColumnConstraints();
    colField.setPercentWidth(44);
    colField.setHgrow(Priority.ALWAYS);

    final ColumnConstraints colToolbar = new ColumnConstraints();
    colToolbar.setPercentWidth(28);
    colToolbar.setHgrow(Priority.NEVER);

    grid.getColumnConstraints().addAll(colLabel, colIcon, colField, colToolbar);

    grid.add(wiki.label, 0, 0);
    grid.add(wiki.icon, 1, 0);
    grid.add(wiki.comboBox, 2, 0);
    grid.add(wiki.toolBar, 3, 0);

    grid.add(language.label, 0, 1);
    grid.add(language.icon, 1, 1);
    grid.add(language.comboBox, 2, 1);
    grid.add(language.toolBar, 3, 1);

    grid.add(user.label, 0, 2);
    grid.add(user.icon, 1, 2);
    grid.add(user.comboBox, 2, 2);
    grid.add(user.toolBar, 3, 2);

    grid.add(password.label, 0, 3);
    grid.add(password.icon, 1, 3);
    grid.add(password.field, 2, 3);
    grid.add(password.toolBar, 3, 3);
    return grid;
  }

  private HBox createButtonsPanel(
      final WikiInput wiki, final UserInput user, final PasswordInput password) {
    final HBox buttons = new HBox(10);
    buttons.setAlignment(Pos.CENTER);

    final Button loginButton = new Button("Login");
    loginButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(loginButton, Priority.ALWAYS);
    loginButton.setOnAction(_ -> handleLogin(wiki, user, password));

    final Button demoButton = new Button("Demo");
    demoButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(demoButton, Priority.ALWAYS);
    demoButton.setOnAction(_ -> handleDemo(wiki, user));

    buttons.getChildren().addAll(loginButton, demoButton);
    return buttons;
  }

  private ToolBar createFeedbacksToolbar() {
    final FeedbacksToolBar feedbacks =
        new FeedbacksToolBar(
            imageLoader,
            services.desktopService(),
            services.urlService(),
            services.saveWindowsPositionAction());
    feedbacks.disableProperty().bind(loading);
    return feedbacks;
  }

  private void handleLogin(
      final WikiInput wiki, final UserInput user, final PasswordInput password) {
    final WikiDefinition selectedWiki = wiki.getSelectedWiki().orElse(null);
    if (selectedWiki == null) {
      showWarning("Missing wiki", "You must select a wiki before login!");
      return;
    }
    final String userVal = user.getUser();
    if (userVal.isEmpty()) {
      showWarning("Missing username", "You must input your username before login!");
      return;
    }
    final char[] passwordVal = password.getPassword();
    if (passwordVal.length == 0) {
      showWarning(
          "Missing password",
          "You must input your password before login!\nIf you prefer to test WPCleaner first, you can use the Demo mode.");
      return;
    }

    loading.set(true);
    final Thread thread = new Thread(() -> processLogin(selectedWiki, userVal, passwordVal));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UseVarargs"})
  private void processLogin(
      final WikiDefinition selectedWiki, final String userVal, final char[] passwordVal) {
    try {
      final LoginProcessor.Input input =
          LoginProcessor.Input.forLogin(selectedWiki, userVal, passwordVal);
      services.loginProcessor().execute(input, progressTracker);
      Platform.runLater(
          () -> {
            loading.set(false);
            displayMainWindow();
          });
    } catch (final Exception e) {
      Platform.runLater(
          () -> {
            loading.set(false);
            showError(
                "Login Failed",
                "An error occurred during login",
                Objects.requireNonNullElseGet(e.getMessage(), e::toString));
          });
    }
  }

  private void handleDemo(final WikiInput wiki, final UserInput user) {
    final WikiDefinition selectedWiki = wiki.getSelectedWiki().orElse(null);
    if (selectedWiki == null) {
      showWarning("Missing wiki", "You must select a wiki before starting demo mode!");
      return;
    }
    final String userVal = user.getUser();
    if (userVal.isEmpty()) {
      showWarning("Missing username", "You must input your username before starting demo mode!");
      return;
    }

    loading.set(true);
    final Thread thread = new Thread(() -> processDemo(selectedWiki, userVal));
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void processDemo(final WikiDefinition selectedWiki, final String userVal) {
    try {
      final LoginProcessor.Input input = LoginProcessor.Input.forDemo(selectedWiki, userVal);
      services.loginProcessor().execute(input, progressTracker);
      Platform.runLater(
          () -> {
            loading.set(false);
            displayMainWindow();
          });
    } catch (final Exception e) {
      Platform.runLater(
          () -> {
            loading.set(false);
            showError(
                "Demo Mode Failed",
                "An error occurred during demo startup",
                Objects.requireNonNullElseGet(e.getMessage(), e::toString));
          });
    }
  }

  private void displayMainWindow() {
    services.main().displayMainWindow();
    close();
  }

  private void showWarning(final String title, final String content) {
    final Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  private void showError(final String title, final String header, final String content) {
    final Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
