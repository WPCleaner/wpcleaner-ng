package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.recentchanges.ApiRecentChanges;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.application.gui.swing.core.window.SaveWindowsPositionAction;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxRecentChangesWindowServices(
    ApiRecentChanges apiRecentChanges,
    NamespaceRepository namespaceRepository,
    TagRepository tagRepository,
    CurrentUserService user,
    ImageLoader imageLoader,
    DesktopService desktopService,
    UrlService urlService,
    SaveWindowsPositionAction saveWindowsPositionAction,
    WindowsSettingsManager windowsSettings) {}
