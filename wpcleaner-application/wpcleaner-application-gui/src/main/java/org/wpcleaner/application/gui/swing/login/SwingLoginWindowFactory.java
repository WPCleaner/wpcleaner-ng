package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.gui.core.factory.LoginWindowFactory;
import org.wpcleaner.application.gui.core.url.UrlService;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

@Service
public class SwingLoginWindowFactory implements LoginWindowFactory {

  private final KnownDefinitions knownDefinitions;
  private final SwingCoreServices swingCoreServices;
  private final UrlService urlService;

  public SwingLoginWindowFactory(
      final KnownDefinitions knownDefinitions,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    this.knownDefinitions = knownDefinitions;
    this.swingCoreServices = swingCoreServices;
    this.urlService = urlService;
  }

  @Override
  public void displayLoginWindow() {
    EventQueue.invokeLater(
        () -> SwingLoginWindow.create(knownDefinitions, swingCoreServices, urlService));
  }
}
