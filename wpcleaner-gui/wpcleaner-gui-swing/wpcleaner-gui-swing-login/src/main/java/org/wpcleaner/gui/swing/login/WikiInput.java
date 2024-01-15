package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.event.ActionListener;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiWarning;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.core.action.SimpleAction;
import org.wpcleaner.gui.swing.core.action.ComponentActionListener;
import org.wpcleaner.gui.swing.core.action.ShowMessageAction;
import org.wpcleaner.gui.swing.core.action.SimpleToComponentAction;
import org.wpcleaner.gui.swing.core.component.ComponentService;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class WikiInput {

  final WikiComboBox comboBox;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  WikiInput(
      final ActionService actionService,
      final ComponentService componentService,
      final ImageIconLoader imageService,
      final KnownDefinitions knownDefinitions) {
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
    final JButton warning =
        componentService
            .buttons()
            .builder("No warning", false)
            .withIcon(ImageCollection.WARNING, ImageSize.TOOLBAR)
            .build();
    warning.setEnabled(false);
    final JButton otherWiki =
        componentService
            .buttons()
            .builder("Other wiki", false)
            .withIcon(ImageCollection.HELP, ImageSize.TOOLBAR)
            .withAction(
                actionService.openUrl("https://en.wikipedia.org/wiki/Wikipedia:WPCleaner/Wikis"))
            .build();
    final JButton addWiki =
        componentService
            .buttons()
            .builder("Add wiki", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final JButton removeWiki =
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
    comboBox.addActionListener(
        new ComponentActionListener(
            new SimpleToComponentAction(new WikiComboBoxAction(comboBox, warning)), comboBox));
  }

  private static final class WikiComboBoxAction implements SimpleAction {

    private final JButton warningButton;
    private final WikiComboBox comboBox;
    @Nullable private ActionListener showMessageAction;

    public WikiComboBoxAction(final WikiComboBox comboBox, final JButton warningButton) {
      this.comboBox = comboBox;
      this.warningButton = warningButton;
      execute();
    }

    @Override
    public void execute() {
      final Optional<String> text =
          Optional.ofNullable(comboBox.getSelectedItem())
              .filter(WikiDefinition.class::isInstance)
              .map(WikiDefinition.class::cast)
              .map(WikiDefinition::warning)
              .map(WikiWarning::text);
      warningButton.setEnabled(text.isPresent());
      warningButton.setToolTipText(text.isPresent() ? "Warning" : "No warning");
      warningButton.removeActionListener(showMessageAction);
      showMessageAction =
          new ComponentActionListener(
              new ShowMessageAction(
                  text.orElse("No warning"), "Warning", JOptionPane.WARNING_MESSAGE),
              warningButton);
      warningButton.addActionListener(showMessageAction);
    }
  }
}
