package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Window;
import org.springframework.stereotype.Service;

@Service
public class JavaFxWindowsRegistry {

  private final List<Window> windows = new ArrayList<>();

  public void register(final Window window) {
    windows.add(window);
    window.setOnHidden(_ -> windows.remove(window));
  }

  public List<Window> getVisibleWindows() {
    return windows.stream().filter(Window::isShowing).toList();
  }
}
