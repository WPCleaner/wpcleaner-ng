package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public abstract class JavaFxWindow<S extends JavaFxWindowServices> {

  protected final JavaFxImageLoader imageLoader;
  protected final BooleanProperty loading;
  protected final JavaFxProgressTracker progressTracker;
  protected final S services;
  protected final Stage stage;

  protected JavaFxWindow(final S services) {
    this(services, null);
  }

  protected JavaFxWindow(final S services, @Nullable final Stage owner) {
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.loading = new SimpleBooleanProperty(false);
    this.progressTracker = JavaFxProgressTracker.forObservable(loading);
    this.stage = new Stage();
    stage.setTitle("WPCleaner");
    stage.getIcons().clear();
    Arrays.stream(ImageSize.values()).forEach(this::setIcon);
    Optional.ofNullable(owner).ifPresent(stage::initOwner);
  }

  protected void initialize() {
    services.windowsRegistry().register(this);
    final Scene scene = createScene();
    scene.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event -> {
          if (KeyCode.F1.equals(event.getCode())) {
            event.consume();
            displayHelp();
          }
        });
    stage.setScene(scene);
    services.actionServices().positionWindow(this);
  }

  public void displayHelp() {
    services.actionServices().displayHelp(this);
  }

  @Nullable
  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  public String getHelpPage() {
    return null;
  }

  public abstract String getName();

  public Stage getStage() {
    return stage;
  }

  protected abstract Scene createScene();

  public final void showConfirmation(final String content, final Runnable okAction) {
    showConfirmation(GT._T("Confirmation"), content, okAction);
  }

  public final void showConfirmation(
      final String title, final String content, final Runnable okAction) {
    show(Alert.AlertType.CONFIRMATION, title, null, content)
        .filter(ButtonType.OK::equals)
        .ifPresent(_ -> okAction.run());
  }

  public void showError(final String content) {
    showError(null, content);
  }

  public void showError(@Nullable final String header, final String content) {
    show(Alert.AlertType.ERROR, GT._T("Error"), header, content);
  }

  public void showInformation(
      final String title, @Nullable final String header, final String content) {
    show(Alert.AlertType.INFORMATION, title, header, content);
  }

  public final void showWarning(final String title, final String content) {
    show(Alert.AlertType.WARNING, title, null, content);
  }

  private Optional<ButtonType> show(
      final Alert.AlertType type,
      final String title,
      @Nullable final String header,
      final String content) {
    final Alert alert = new Alert(type);
    alert.initOwner(stage);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    return alert.showAndWait();
  }

  private void setIcon(final ImageSize size) {
    imageLoader
        .getImage(ImageCollection.LOGO_WPCLEANER, size)
        .ifPresent(img -> stage.getIcons().add(img));
  }
}
