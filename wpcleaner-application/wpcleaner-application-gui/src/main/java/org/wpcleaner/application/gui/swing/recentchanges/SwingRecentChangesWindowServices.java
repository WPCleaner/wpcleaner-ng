package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.recentchanges.ApiRecentChanges;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.table.ColumnConfigurerFactory;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindowServices;

@Service
public record SwingRecentChangesWindowServices(
    ApiRecentChanges apiRecentChanges,
    ColumnConfigurerFactory columnFactory,
    CurrentUserService user,
    SwingCoreServices swing,
    WindowsSettingsManager windowsSettings)
    implements WPCleanerWindowServices {}
