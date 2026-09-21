package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

@Service
public class JavaFxWindowsRegistry {

  private final List<JavaFxWindow<?>> windows = new ArrayList<>();

  public void register(final JavaFxWindow<?> window) {
    windows.add(window);
    window.getStage().setOnHidden(_ -> windows.remove(window));
  }

  public List<JavaFxWindow<?>> getVisibleWindows() {
    return windows.stream().filter(window -> window.getStage().isShowing()).toList();
  }
}
