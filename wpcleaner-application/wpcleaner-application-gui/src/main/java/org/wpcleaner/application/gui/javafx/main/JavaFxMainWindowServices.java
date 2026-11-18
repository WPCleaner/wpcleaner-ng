package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.linterrors.ApiLintErrors;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.list.search.ApiSearch;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.repository.linter.LinterCategoryRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.factory.AnalysisWindowFactory;
import org.wpcleaner.application.gui.core.factory.PageListWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxFileChooser;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowFactory;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.server.CheckWikiServer;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxMainWindowServices(
    JavaFxActionServices actionServices,
    PageAnalysisFactory analysisFactory,
    AnalysisWindowFactory analysisWindowFactory,
    ApiLintErrors apiLintErrors,
    ApiRandom apiRandom,
    ApiRevisions apiRevisions,
    ApiSearch apiSearch,
    CheckAlgorithmRegistry checkAlgorithmRegistry,
    CheckWikiServer checkWikiServer,
    JavaFxFileChooser fileChooser,
    ImageLoader imageLoader,
    InterestingSettingsManager interestingSettings,
    LinterCategoryRepository linterCategories,
    NamespaceRepository namespaceRepository,
    PageListWindowFactory pageListWindowFactory,
    JavaFxRecentChangesWindowFactory recentChangesWindowFactory,
    UrlService urlService,
    CurrentUserService user,
    JavaFxWindowsRegistry windowsRegistry)
    implements JavaFxWindowServices {}
