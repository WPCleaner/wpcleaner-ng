package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.application.gui.swing.core.window.SaveWindowsPositionAction;
import org.wpcleaner.application.gui.swing.recentchanges.SwingRecentChangesWindowFactory;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxMainWindowServices(
    CurrentUserService user,
    WindowsSettingsManager windowsSettings,
    InterestingSettingsManager interestingSettings,
    SwingRecentChangesWindowFactory recentChangesWindowFactory,
    ImageLoader imageLoader,
    DesktopService desktopService,
    UrlService urlService,
    SaveWindowsPositionAction saveWindowsPositionAction) {}
