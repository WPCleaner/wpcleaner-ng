package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Optional;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiWarning;
import org.wpcleaner.application.gui.core.action.ActionService;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class WikiInput {

  private static final String NO_WARNING = "No warning";
  private static final String WARNING = "Warning";

  final WikiComboBox comboBox;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  WikiInput(final SwingCoreServices swingCoreServices, final KnownDefinitions knownDefinitions) {
    final ActionService actionService = swingCoreServices.action().action();
    final ComponentService componentService = swingCoreServices.component();
    final ImageIconLoader imageService = swingCoreServices.image();
    icon =
        componentService
            .labels()
            .builder("Wiki", false)
            .withIcon(ImageCollection.LOGO_MEDIAWIKI, ImageSize.LABEL)
            .build();
    comboBox = new WikiComboBox(knownDefinitions);
    comboBox.setRenderer(new WikiListCellRenderer(imageService));
    label =
        componentService
            .labels()
            .builder("Wiki", true)
            .withHorizontalAlignment(SwingConstants.TRAILING)
            .withComponent(comboBox)
            .build();
    final AbstractButton warning =
        componentService
            .buttons()
            .builder(NO_WARNING, false)
            .withIcon(ImageCollection.WARNING, ImageSize.TOOLBAR)
            .build();
    warning.setEnabled(false);
    final AbstractButton otherWiki =
        componentService
            .buttons()
            .builder("Other wiki", false)
            .withIcon(ImageCollection.HELP, ImageSize.TOOLBAR)
            .withAction(
                actionService.openUrl("https://en.wikipedia.org/wiki/Wikipedia:WPCleaner/Wikis"))
            .build();
    final AbstractButton addWiki =
        componentService
            .buttons()
            .builder("Add wiki", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final AbstractButton removeWiki =
        componentService
            .buttons()
            .builder("Remove wiki", false)
            .withIcon(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    toolBar =
        componentService
            .toolBars()
            .builder()
            .withComponent(warning)
            .withComponent(otherWiki)
            .withComponent(addWiki)
            .withComponent(removeWiki)
            .build();
    final WikiComboBoxAction action = new WikiComboBoxAction(comboBox, warning);
    comboBox.addActionListener(_ -> action.updateWarningButton());
  }

  public Optional<WikiDefinition> getSelectedWiki() {
    final int selectedIndex = comboBox.getSelectedIndex();
    if (selectedIndex < 0) {
      return Optional.empty();
    }
    return Optional.of(comboBox.getItemAt(selectedIndex));
  }

  public void addItemListener(final ItemListener itemListener) {
    comboBox.addItemListener(itemListener);
    final Object selectedObject = comboBox.getSelectedItem();
    if (selectedObject != null) {
      itemListener.itemStateChanged(
          new ItemEvent(comboBox, ItemEvent.ITEM_FIRST, selectedObject, ItemEvent.SELECTED));
    }
  }

  private record WikiComboBoxAction(WikiComboBox comboBox, AbstractButton warningButton) {

    private WikiComboBoxAction(final WikiComboBox comboBox, final AbstractButton warningButton) {
      this.comboBox = comboBox;
      this.warningButton = warningButton;
      this.warningButton.addActionListener(_ -> displayWarning());
      updateWarningButton();
    }

    void updateWarningButton() {
      final boolean text = getText().isPresent();
      warningButton.setEnabled(text);
      warningButton.setToolTipText(text ? WARNING : NO_WARNING);
    }

    private void displayWarning() {
      getText()
          .ifPresent(
              text ->
                  JOptionPane.showMessageDialog(
                      warningButton, text, WARNING, JOptionPane.WARNING_MESSAGE));
    }

    private Optional<String> getText() {
      return Optional.ofNullable(comboBox.getSelectedItem())
          .filter(WikiDefinition.class::isInstance)
          .map(WikiDefinition.class::cast)
          .map(WikiDefinition::warning)
          .map(WikiWarning::text);
    }
  }
}
