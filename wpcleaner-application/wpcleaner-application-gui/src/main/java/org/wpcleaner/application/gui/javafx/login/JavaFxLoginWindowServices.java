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
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.main.JavaFxMainWindowFactory;
import org.wpcleaner.lib.image.ImageLoader;

@Service
public record JavaFxLoginWindowServices(
    JavaFxActionServices actionServices,
    CredentialsProvider credentialsProvider,
    ImageLoader imageLoader,
    KnownDefinitions knownDefinitions,
    LoginProcessor loginProcessor,
    JavaFxMainWindowFactory main,
    UrlService urlService,
    JavaFxWindowsRegistry windowsRegistry) {}
