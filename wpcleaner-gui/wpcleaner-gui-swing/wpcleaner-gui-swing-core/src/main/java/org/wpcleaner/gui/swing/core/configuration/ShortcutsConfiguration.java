package org.wpcleaner.gui.swing.core.configuration;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class ShortcutsConfiguration {

  public ShortcutsConfiguration() {
    final int menuShortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    if (menuShortcut != InputEvent.META_DOWN_MASK) {
      return;
    }
    if (!(UIManager.get("TextField.focusInputMap") instanceof InputMap im)) {
      return;
    }
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut), DefaultEditorKit.copyAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcut), DefaultEditorKit.pasteAction);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, menuShortcut), DefaultEditorKit.cutAction);
  }
}
