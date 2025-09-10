package org.wpcleaner.application.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JButton;
import javax.swing.JToolBar;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.action.ActionService;
import org.wpcleaner.application.gui.swing.login.FeedbackAction;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

@Service
public class ToolBarService {

  private final ActionService actionService;
  private final ButtonService buttonService;
  private final MenuItemService menuItemService;

  public ToolBarService(
      final ActionService actionService,
      final ButtonService buttonService,
      final MenuItemService menuItemService) {
    this.actionService = actionService;
    this.buttonService = buttonService;
    this.menuItemService = menuItemService;
  }

  public JToolBarBuilder builder() {
    return new JToolBarBuilder();
  }

  public JToolBar feedbacks() {
    final JButton feedbackButton =
        buttonService
            .builder("Feedback", false)
            .withIcon(ImageCollection.HELP_FAQ, ImageSize.BUTTON)
            .withAction(new FeedbackAction(menuItemService))
            .build();
    final JButton optionsButton =
        buttonService
            .builder("Options", false)
            .withIcon(ImageCollection.OPTIONS, ImageSize.BUTTON)
            .withAction(actionService.notImplemented())
            .build();
    final JButton aboutButton =
        buttonService
            .builder("About", false)
            .withIcon(ImageCollection.HELP_ABOUT, ImageSize.BUTTON)
            .withAction(actionService.notImplemented())
            .build();
    return builder()
        .withComponent(feedbackButton)
        .withComponent(optionsButton)
        .withComponent(aboutButton)
        .build();
  }
}
