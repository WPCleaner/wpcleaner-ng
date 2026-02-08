package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

public interface WPCleanerWindowServices {

  WindowsSettingsManager windowsSettings();

  SwingCoreServices swing();
}
