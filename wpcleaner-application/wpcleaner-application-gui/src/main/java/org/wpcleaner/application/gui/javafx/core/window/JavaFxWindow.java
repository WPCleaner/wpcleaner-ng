package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
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
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.loading = new SimpleBooleanProperty(false);
    this.progressTracker = JavaFxProgressTracker.forObservable(loading);
    this.stage = new Stage();
    stage.setTitle("WPCleaner");
    stage.getIcons().clear();
    Arrays.stream(ImageSize.values()).forEach(this::setIcon);
  }

  protected void initialize() {
    services.windowsRegistry().register(this);
    stage.setScene(createScene());
    services.actionServices().positionWindow(this);
  }

  public abstract String getName();

  public Stage getStage() {
    return stage;
  }

  protected abstract Scene createScene();

  protected void showWarning(final String title, final String content) {
    final Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  protected void showError(final String title, final String header, final String content) {
    final Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  private void setIcon(final ImageSize size) {
    imageLoader
        .getImage(ImageCollection.LOGO_WPCLEANER, size)
        .ifPresent(img -> stage.getIcons().add(img));
  }
}
