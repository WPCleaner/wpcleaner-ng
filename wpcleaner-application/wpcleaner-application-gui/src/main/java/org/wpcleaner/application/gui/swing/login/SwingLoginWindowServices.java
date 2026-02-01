package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.settings.graphical.GraphicalSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindowServices;

@Service
public record SwingLoginWindowServices(
    DemoAction demo,
    GraphicalSettingsManager graphicalSettings,
    KnownDefinitions knownDefinitions,
    LoginAction login,
    MainWindowFactory main,
    SwingCoreServices swing,
    UrlService url)
    implements WPCleanerWindowServices {}
