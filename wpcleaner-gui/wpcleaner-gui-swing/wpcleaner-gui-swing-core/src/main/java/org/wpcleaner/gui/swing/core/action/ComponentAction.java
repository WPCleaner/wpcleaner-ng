package org.wpcleaner.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;

@FunctionalInterface
public interface ComponentAction {

  void execute(Component component);
}
