package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JButton;
import org.springframework.stereotype.Service;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

@Service
public class ButtonService {

  private final ActionService actionService;
  private final ImageIconLoader imageService;

  ButtonService(final ActionService actionService, final ImageIconLoader imageService) {
    this.actionService = actionService;
    this.imageService = imageService;
  }

  public JButtonBuilder builder(final String message, final boolean displayMessage) {
    return new JButtonBuilder(imageService, message, displayMessage);
  }

  public JButton about() {
    return builder("About", false)
        .withIcon(ImageCollection.HELP_ABOUT, ImageSize.BUTTON)
        .withAction(actionService.notImplemented())
        .build();
  }

  public JButton options() {
    return builder("Options", false)
        .withIcon(ImageCollection.OPTIONS, ImageSize.BUTTON)
        .withAction(actionService.notImplemented())
        .build();
  }
}
