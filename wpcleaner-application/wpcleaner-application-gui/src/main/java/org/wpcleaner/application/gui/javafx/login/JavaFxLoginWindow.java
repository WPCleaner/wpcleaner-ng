/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.Arrays;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.base.processor.LoginResult;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.control.FeedbacksToolBar;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public final class JavaFxLoginWindow extends JavaFxWindow<JavaFxLoginWindowServices> {

  public JavaFxLoginWindow(final JavaFxLoginWindowServices services) {
    super(services);
    initialize();
    stage.setOnCloseRequest(
        event -> {
          event.consume();
          JavaFxInitializer.exit(services.windowsRegistry());
        });
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "Login";
  }

  @Override
  public String getName() {
    return "login";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(6, 15, 6, 15));
    mainContainer.setAlignment(Pos.CENTER);

    final WikiInput wiki = new WikiInput(this, services, imageLoader);
    final LanguageInput language = new LanguageInput(imageLoader, services.actionServices());
    final UserInput user = new UserInput(imageLoader, services.actionServices());
    final PasswordInput password = new PasswordInput(imageLoader, services.actionServices());

    final GridPane grid = createFormGrid(wiki, language, user, password);
    final HBox buttons = createButtonsPanel(wiki, user, password);
    final ToolBar feedbacks = createFeedbacksToolbar();

    mainContainer.getChildren().addAll(grid, buttons, feedbacks);

    grid.disableProperty().bind(loading);
    buttons.disableProperty().bind(loading);

    root.getChildren().addAll(mainContainer, progressOverlay);

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
    return new Scene(root, 650, 240);
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

    final Button loginButton = new Button(GT._T("Login"));
    loginButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(loginButton, Priority.ALWAYS);
    loginButton.setOnAction(_ -> handleLogin(wiki, user, password));

    final Button demoButton = new Button(GT._T("Demo"));
    demoButton.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(demoButton, Priority.ALWAYS);
    demoButton.setOnAction(_ -> handleDemo(wiki, user));

    buttons.getChildren().addAll(loginButton, demoButton);
    return buttons;
  }

  private ToolBar createFeedbacksToolbar() {
    final FeedbacksToolBar feedbacks =
        new FeedbacksToolBar(services.actionServices(), imageLoader, services.urlService());
    feedbacks.disableProperty().bind(loading);
    return feedbacks;
  }

  private void handleLogin(
      final WikiInput wiki, final UserInput user, final PasswordInput password) {
    final WikiDefinition selectedWiki = wiki.getSelectedWiki().orElse(null);
    if (selectedWiki == null) {
      showWarning(GT._T("Missing wiki"), GT._T("You must select a wiki before login!"));
      return;
    }
    final String userVal = user.getUser();
    if (userVal.isEmpty()) {
      showWarning(GT._T("Missing username"), GT._T("You must input your username before login!"));
      return;
    }
    final char[] passwordVal = password.getPassword();
    if (passwordVal.length == 0) {
      showWarning(
          GT._T("Missing password"),
          GT._T("You must input your password before login!")
              + "\n"
              + GT._T("If you prefer to test WPCleaner first, you can use the Demo mode."));
      return;
    }

    executeAsync(
        new LoginTask(selectedWiki, userVal, passwordVal),
        _ -> displayMainWindow(),
        e ->
            showError(
                GT._T("An error occurred during login"),
                Objects.requireNonNullElseGet(e.getMessage(), e::toString)));
  }

  private class LoginTask implements LongRunningTask<LoginResult> {

    private final WikiDefinition selectedWiki;
    private final String userVal;
    private final char[] passwordVal;

    @SuppressWarnings("PMD.UseVarargs")
    LoginTask(final WikiDefinition selectedWiki, final String userVal, final char[] passwordVal) {
      this.selectedWiki = selectedWiki;
      this.userVal = userVal;
      this.passwordVal = Arrays.copyOf(passwordVal, passwordVal.length);
    }

    @Override
    public LoginResult call(final ProgressTracker tracker) {
      final LoginProcessor.Input input =
          LoginProcessor.Input.forLogin(selectedWiki, userVal, passwordVal);
      return services.loginProcessor().execute(input, tracker);
    }
  }

  private void handleDemo(final WikiInput wiki, final UserInput user) {
    final WikiDefinition selectedWiki = wiki.getSelectedWiki().orElse(null);
    if (selectedWiki == null) {
      showWarning(
          GT._T("Missing wiki"), GT._T("You must select a wiki before starting demo mode!"));
      return;
    }
    final String userVal = user.getUser();
    if (userVal.isEmpty()) {
      showWarning(
          GT._T("Missing username"),
          GT._T("You must input your username before starting demo mode!"));
      return;
    }

    executeAsync(
        new DemoTask(selectedWiki, userVal),
        _ -> displayMainWindow(),
        e ->
            showError(
                GT._T("An error occurred during demo startup"),
                Objects.requireNonNullElseGet(e.getMessage(), e::toString)));
  }

  private class DemoTask implements LongRunningTask<LoginResult> {

    private final WikiDefinition selectedWiki;
    private final String userVal;

    DemoTask(final WikiDefinition selectedWiki, final String userVal) {
      this.selectedWiki = selectedWiki;
      this.userVal = userVal;
    }

    @Override
    public LoginResult call(final ProgressTracker tracker) {
      final LoginProcessor.Input input = LoginProcessor.Input.forDemo(selectedWiki, userVal);
      return services.loginProcessor().execute(input, tracker);
    }
  }

  private void displayMainWindow() {
    services.main().displayMainWindow();
    stage.close();
  }
}
