package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.factory.AnalysisWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowFactory;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxMainWindowServices(
    JavaFxActionServices actionServices,
    AnalysisWindowFactory analysisWindowFactory,
    ApiRandom apiRandom,
    ApiRevisions apiRevisions,
    ImageLoader imageLoader,
    InterestingSettingsManager interestingSettings,
    JavaFxRecentChangesWindowFactory recentChangesWindowFactory,
    UrlService urlService,
    CurrentUserService user,
    JavaFxWindowsRegistry windowsRegistry)
    implements JavaFxWindowServices {}
