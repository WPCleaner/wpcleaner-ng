package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.analysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxAnalysisWindowServices(
    JavaFxActionServices actionServices,
    ApiRevisions apiRevisions,
    PageSyntaxColorizer colorizer,
    ImageLoader imageLoader,
    PageAnalysisFactory pageAnalysisFactory,
    JavaFxStylePropertiesRegistry styleRegistry,
    CurrentUserService user,
    JavaFxWindowsRegistry windowsRegistry) {}
