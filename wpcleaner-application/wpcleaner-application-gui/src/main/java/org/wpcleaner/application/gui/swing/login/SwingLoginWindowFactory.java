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

  private final DemoAction demoAction;
  private final KnownDefinitions knownDefinitions;
  private final LoginAction loginAction;
  private final MainWindowFactory mainWindowFactory;
  private final SwingCoreServices swingCoreServices;
  private final UrlService urlService;

  public SwingLoginWindowFactory(
      final DemoAction demoAction,
      final KnownDefinitions knownDefinitions,
      final LoginAction loginAction,
      final MainWindowFactory mainWindowFactory,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    this.demoAction = demoAction;
    this.knownDefinitions = knownDefinitions;
    this.loginAction = loginAction;
    this.mainWindowFactory = mainWindowFactory;
    this.swingCoreServices = swingCoreServices;
    this.urlService = urlService;
  }

  @Override
  public void displayLoginWindow() {
    EventQueue.invokeLater(
        () ->
            SwingLoginWindow.create(
                demoAction,
                knownDefinitions,
                loginAction,
                mainWindowFactory,
                swingCoreServices,
                urlService));
  }
}
