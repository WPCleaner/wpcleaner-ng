/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxSaveWindowsPositionAction;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.main.JavaFxMainWindowFactory;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxLoginWindowServices(
    CredentialsProvider credentialsProvider,
    KnownDefinitions knownDefinitions,
    LoginProcessor loginProcessor,
    JavaFxMainWindowFactory main,
    DesktopService desktopService,
    ImageLoader imageLoader,
    WindowsSettingsManager windowsSettings,
    UrlService urlService,
    JavaFxSaveWindowsPositionAction saveWindowsPositionAction,
    JavaFxWindowsRegistry windowsRegistry) {}
