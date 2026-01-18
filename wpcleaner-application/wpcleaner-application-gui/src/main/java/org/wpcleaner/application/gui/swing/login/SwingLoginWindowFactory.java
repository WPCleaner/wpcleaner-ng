package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.factory.LoginWindowFactory;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

@Service
public class SwingLoginWindowFactory implements LoginWindowFactory {

  private final KnownDefinitions knownDefinitions;
  private final SwingCoreServices swingCoreServices;
  private final UrlService urlService;
  private final MainWindowFactory mainWindowFactory;

  public SwingLoginWindowFactory(
      final KnownDefinitions knownDefinitions,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService,
      final MainWindowFactory mainWindowFactory) {
    this.knownDefinitions = knownDefinitions;
    this.swingCoreServices = swingCoreServices;
    this.urlService = urlService;
    this.mainWindowFactory = mainWindowFactory;
  }

  @Override
  public void displayLoginWindow() {
    EventQueue.invokeLater(
        () ->
            SwingLoginWindow.create(
                knownDefinitions, swingCoreServices, urlService, mainWindowFactory));
  }
}
