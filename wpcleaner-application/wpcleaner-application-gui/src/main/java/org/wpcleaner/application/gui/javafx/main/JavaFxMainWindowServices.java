package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxSaveWindowsPositionAction;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowFactory;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxMainWindowServices(
    CurrentUserService user,
    WindowsSettingsManager windowsSettings,
    InterestingSettingsManager interestingSettings,
    JavaFxRecentChangesWindowFactory recentChangesWindowFactory,
    ImageLoader imageLoader,
    DesktopService desktopService,
    UrlService urlService,
    JavaFxSaveWindowsPositionAction saveWindowsPositionAction,
    JavaFxWindowsRegistry windowsRegistry) {}
