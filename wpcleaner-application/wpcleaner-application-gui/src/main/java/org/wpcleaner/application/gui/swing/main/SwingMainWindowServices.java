package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.application.gui.settings.graphical.GraphicalSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindowServices;

@Service
public record SwingMainWindowServices(
    CurrentUserService user, GraphicalSettingsManager graphicalSettings, SwingCoreServices swing)
    implements WPCleanerWindowServices {}
