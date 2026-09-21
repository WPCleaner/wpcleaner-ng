package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageLoader;

public interface JavaFxWindowServices {

  JavaFxActionServices actionServices();

  ImageLoader imageLoader();

  JavaFxWindowsRegistry windowsRegistry();
}
