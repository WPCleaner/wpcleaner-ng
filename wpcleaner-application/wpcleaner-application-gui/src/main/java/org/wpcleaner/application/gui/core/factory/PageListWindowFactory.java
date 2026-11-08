package org.wpcleaner.application.gui.core.factory;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Consumer;
import org.wpcleaner.api.progress.LongRunningTask;

public interface PageListWindowFactory {

  void displayPageListWindow(
      String title, LongRunningTask<List<String>> pagesTask, Consumer<String> pageAction);
}
