package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WindowsRegistry {

  private final List<Window> windows = new ArrayList<>();
  private final Remover remover = new Remover();

  public void register(final Window window) {
    windows.add(window);
    window.addWindowListener(remover);
  }

  public List<Window> getVisibleWindows() {
    return windows.stream().filter(Window::isVisible).toList();
  }

  private void unregister(final Window window) {
    windows.remove(window);
    window.removeWindowListener(remover);
  }

  private final class Remover extends WindowAdapter {

    @Override
    public void windowClosing(final WindowEvent e) {
      super.windowClosing(e);
      unregister(e.getWindow());
    }
  }
}
