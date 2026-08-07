package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.recentchanges.ApiRecentChanges;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.application.gui.settings.recentchanges.RecentChangesSettingsManager;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxRecentChangesWindowServices(
    JavaFxActionServices actionServices,
    ApiRecentChanges apiRecentChanges,
    ApiRevisions apiRevisions,
    ImageLoader imageLoader,
    NamespaceRepository namespaceRepository,
    RecentChangesSettingsManager recentChangesSettingsManager,
    JavaFxStylePropertiesRegistry stylePropertiesRegistry,
    TagRepository tagRepository,
    UrlService urlService,
    CurrentUserService user,
    JavaFxWindowsRegistry windowsRegistry) {}
