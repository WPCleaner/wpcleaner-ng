package org.wpcleaner.application.gui.core.factory;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.wiki.definition.WikiDefinition;

public interface MainWindowFactory {
  void displayMainWindow(String user, WikiDefinition wiki);
}
