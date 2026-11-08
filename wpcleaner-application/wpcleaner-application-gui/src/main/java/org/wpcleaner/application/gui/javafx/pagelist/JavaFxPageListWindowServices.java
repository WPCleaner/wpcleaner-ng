package org.wpcleaner.application.gui.javafx.pagelist;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindowServices;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxPageListWindowServices(
    JavaFxActionServices actionServices,
    ImageLoader imageLoader,
    JavaFxWindowsRegistry windowsRegistry)
    implements JavaFxWindowServices {}
