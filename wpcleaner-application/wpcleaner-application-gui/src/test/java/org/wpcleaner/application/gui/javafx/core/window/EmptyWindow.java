package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;

public final class EmptyWindow<S extends JavaFxWindowServices> extends JavaFxWindow<S> {

  public EmptyWindow(final S services) {
    super(services);
    initialize();
  }

  @NonNull
  @Override
  public String getName() {
    return "emptyWindow";
  }

  @NonNull
  @Override
  protected Scene createScene() {
    return new Scene(new StackPane(), 100, 100);
  }
}
