package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.component.ComponentServices;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class LanguageInput {

  final LanguageComboBox comboBox;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  LanguageInput(
      final ActionService actionService,
      final ComponentServices componentServices,
      final ImageIconLoader imageService) {
    icon =
        componentServices
            .labels()
            .builder("Language", false)
            .withIcon(ImageCollection.LANGUAGE, ImageSize.LABEL)
            .build();
    comboBox = new LanguageComboBox();
    comboBox.setRenderer(new LanguageListCellRenderer(imageService));
    label =
        componentServices
            .labels()
            .builder("Language", true)
            .withHorizontalAlignment(SwingConstants.TRAILING)
            .withComponent(comboBox)
            .build();
    final JButton addLanguageButton =
        componentServices
            .buttons()
            .builder("Add language", false)
            .withIcon(ImageCollection.LANGUAGE_ADD, ImageSize.TOOLBAR)
            .withAction(
                actionService.openUrl("https://translatewiki.net/wiki/Translating:WPCleaner"))
            .build();
    toolBar = componentServices.toolBars().builder().withComponent(addLanguageButton).build();
  }
}
