package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JMenuItem;
import org.springframework.stereotype.Service;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.core.url.UrlService;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

@Service
public class MenuItemService {

  private final ActionService actionService;
  private final ImageIconLoader imageService;
  private final UrlService urlService;

  MenuItemService(
      final ActionService actionService,
      final ImageIconLoader imageService,
      final UrlService urlService) {
    this.actionService = actionService;
    this.imageService = imageService;
    this.urlService = urlService;
  }

  public JMenuItemBuilder builder(final String message) {
    return new JMenuItemBuilder(imageService, message);
  }

  public JMenuItem askQuestion() {
    return builder("Ask a question")
        .withIcon(ImageCollection.HELP, ImageSize.MENU)
        .withAction(actionService.openUrl(urlService.askQuestion()))
        .build();
  }

  public JMenuItem help() {
    return builder("Help")
        .withIcon(ImageCollection.HELP, ImageSize.MENU)
        .withAction(actionService.openUrl(urlService.help()))
        .build();
  }

  public JMenuItem reportBug() {
    return builder("Report bug")
        .withIcon(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .withAction(actionService.openUrl(urlService.reportBug()))
        .build();
  }

  public JMenuItem requestFeature() {
    return builder("Request new feature")
        .withIcon(ImageCollection.LOGO_PHABRICATOR, ImageSize.MENU)
        .withAction(actionService.openUrl(urlService.requestFeature()))
        .build();
  }
}
