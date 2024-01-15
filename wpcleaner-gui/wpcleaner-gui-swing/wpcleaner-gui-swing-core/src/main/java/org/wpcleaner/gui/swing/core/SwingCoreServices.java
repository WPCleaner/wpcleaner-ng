package org.wpcleaner.gui.swing.core;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.gui.swing.core.action.SwingActionService;
import org.wpcleaner.gui.swing.core.component.ComponentService;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.gui.swing.core.layout.GridBagLayoutService;

@Service
public record SwingCoreServices(
    SwingActionService action,
    ComponentService component,
    ImageIconLoader image,
    GridBagLayoutService layout) {}
