package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.SwingCoreServices;
import org.wpcleaner.gui.swing.core.component.ComponentServices;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.gui.swing.core.layout.GridBagLayoutService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class SwingLoginWindow extends JFrame {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final KnownDefinitions knownDefinitions;
  private final ActionService actionService;
  private final ComponentServices componentService;
  private final ImageIconLoader imageService;
  private final GridBagLayoutService layoutService;

  public static void create(
      final KnownDefinitions knownDefinitions, final SwingCoreServices swingCoreServices) {
    final SwingLoginWindow window = new SwingLoginWindow(knownDefinitions, swingCoreServices);
    window.initialize();
  }

  private SwingLoginWindow(
      final KnownDefinitions knownDefinitions, final SwingCoreServices swingCoreServices) {
    super("WPCleaner");
    this.knownDefinitions = knownDefinitions;
    this.actionService = swingCoreServices.action().action();
    this.componentService = swingCoreServices.component();
    this.imageService = swingCoreServices.image();
    this.layoutService = swingCoreServices.layout();
  }

  private void initialize() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    imageService
        .getImage(ImageCollection.LOGO_WPCLEANER, ImageSize.ICON)
        .map(ImageIcon::getImage)
        .ifPresent(this::setIconImage);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = layoutService.initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    final WikiInput wiki =
        new WikiInput(actionService, componentService, imageService, knownDefinitions);
    addLine(panel, constraints, wiki.label, wiki.icon, wiki.comboBox, wiki.toolBar);

    final LanguageInput language = new LanguageInput(actionService, componentService, imageService);
    addLine(panel, constraints, language.label, language.icon, language.comboBox, language.toolBar);

    layoutService.addFillingPanelBelow(panel);
    getContentPane().add(panel);
    pack();
    setVisible(true);
  }

  private void addLine(
      final JPanel panel,
      final GridBagConstraints constraints,
      final JLabel label,
      final JLabel icon,
      final JComponent selector,
      final JToolBar toolBar) {
    constraints.gridx = 0;
    panel.add(label, constraints);
    constraints.gridx++;
    panel.add(icon, constraints);
    constraints.gridx++;
    constraints.weightx = 1;
    panel.add(selector, constraints);
    constraints.gridx++;
    constraints.weightx = 0;
    panel.add(toolBar, constraints);
    constraints.gridy++;
  }
}
