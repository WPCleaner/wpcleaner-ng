package org.wpcleaner.application.gui.swing.core.layout;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.function.Consumer;

public record GridBagComponent(Component component, Consumer<GridBagConstraints> adapter) {
  public static GridBagComponent of(final Component component) {
    return of(component, constraints -> {});
  }

  public static GridBagComponent of(
      final Component component, final Consumer<GridBagConstraints> adapter) {
    return new GridBagComponent(component, adapter);
  }
}
