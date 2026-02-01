package org.wpcleaner.application.gui.swing.core;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.swing.core.action.SwingActionService;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.application.gui.swing.core.dialog.ErrorDialogService;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.application.gui.swing.core.layout.GridBagLayoutService;
import org.wpcleaner.application.gui.swing.core.window.WindowsRegistry;

@Service
public record SwingCoreServices(
    SwingActionService action,
    ComponentService component,
    ErrorDialogService errorDialog,
    ImageIconLoader image,
    GridBagLayoutService layout,
    WindowsRegistry windows) {}
