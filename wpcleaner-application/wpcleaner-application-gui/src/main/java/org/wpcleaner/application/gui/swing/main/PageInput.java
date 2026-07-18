package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.action.ActionService;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PageInput {

  final JComboBox<String> comboBox;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  PageInput(
      final WikiDefinition wiki,
      final SwingCoreServices swingCoreServices,
      final InterestingSettingsManager settingsManager) {
    final ActionService actionService = swingCoreServices.action().action();
    final ComponentService componentService = swingCoreServices.component();
    icon =
        componentService
            .labels()
            .builder("Page name", false)
            .withIcon(ImageCollection.PAGE, ImageSize.LABEL)
            .build();
    comboBox = new JComboBox<>();
    comboBox.setEditable(true);
    settingsManager.getCurrentSettings().getByWikiSettings(wiki).stream()
        .flatMap(settings -> settings.pages().stream())
        .forEach(comboBox::addItem);
    label =
        componentService
            .labels()
            .builder("Page", true)
            .withHorizontalAlignment(SwingConstants.TRAILING)
            .withComponent(comboBox)
            .build();
    final AbstractButton addPage =
        componentService
            .buttons()
            .builder("Add page", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final AbstractButton removePage =
        componentService
            .buttons()
            .builder("Forget page", false)
            .withIcon(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    toolBar =
        componentService
            .toolBars()
            .builder()
            .withComponent(addPage)
            .withComponent(removePage)
            .build();
  }

  public String getPage() {
    return Objects.requireNonNullElse(comboBox.getSelectedItem(), "").toString();
  }
}
