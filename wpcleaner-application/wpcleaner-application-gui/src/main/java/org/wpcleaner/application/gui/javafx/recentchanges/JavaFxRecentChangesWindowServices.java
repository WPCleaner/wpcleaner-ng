package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.edit.ApiEdit;
import org.wpcleaner.api.api.query.list.recentchanges.ApiRecentChanges;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindowServices;
import org.wpcleaner.application.gui.settings.recentchanges.RecentChangesSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxRecentChangesWindowServices(
    JavaFxActionServices actionServices,
    ApiEdit apiEdit,
    ApiRecentChanges apiRecentChanges,
    ApiRevisions apiRevisions,
    ApiTokens apiTokens,
    PageSyntaxColorizer colorizer,
    ImageLoader imageLoader,
    NamespaceRepository namespaceRepository,
    PageAnalysisFactory pageAnalysisFactory,
    RecentChangesSettingsManager recentChangesSettingsManager,
    JavaFxStylePropertiesRegistry stylePropertiesRegistry,
    TagRepository tagRepository,
    UrlService urlService,
    CurrentUserService user,
    JavaFxWindowsRegistry windowsRegistry)
    implements JavaFxWindowServices {}
