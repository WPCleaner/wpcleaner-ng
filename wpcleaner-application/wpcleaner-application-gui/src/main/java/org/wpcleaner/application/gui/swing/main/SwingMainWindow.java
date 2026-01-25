package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;
import org.wpcleaner.application.gui.swing.core.layout.GridBagLayoutService;

public final class SwingMainWindow extends JFrame {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final transient ComponentService componentService;
  private final transient ImageIconLoader imageService;
  private final transient GridBagLayoutService layoutService;
  private final transient ConnectedUser user;

  public static void create(final SwingCoreServices swingCoreServices, final ConnectedUser user) {
    final SwingMainWindow window = new SwingMainWindow(swingCoreServices, user);
    window.initialize();
  }

  private SwingMainWindow(final SwingCoreServices swingCoreServices, final ConnectedUser user) {
    super("WPCleaner");
    this.componentService = swingCoreServices.component();
    this.imageService = swingCoreServices.image();
    this.layoutService = swingCoreServices.layout();
    this.user = user;
  }

  private void initialize() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    imageService.setIconImage(this);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = layoutService.initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    addWelcomeMessage(panel, constraints);
    addButtons(panel, constraints);

    layoutService.addFillingPanelBelow(panel);
    getContentPane().add(panel);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void addWelcomeMessage(final JPanel panel, final GridBagConstraints constraints) {
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Welcome %s on WPCleaner Next Generation!".formatted(user.username()),
                SwingConstants.CENTER)));
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "You are currently connected to %s".formatted(user.wiki()),
                SwingConstants.CENTER)));
    if (user.demo()) {
      layoutService.addRow(
          panel,
          constraints,
          GridBagComponent.of(
              new JLabel(
                  "You are currently in demo mode, you won't be able to save your modifications",
                  SwingConstants.CENTER)));
    }
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Your groups: %s"
                    .formatted(user.groups().stream().limit(3).collect(Collectors.joining(", "))),
                SwingConstants.CENTER)));
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Your rights: %s"
                    .formatted(user.rights().stream().limit(3).collect(Collectors.joining(", "))),
                SwingConstants.CENTER)));
  }

  private void addButtons(final JPanel panel, final GridBagConstraints constraints) {
    layoutService.addRowSpanningAllColumns(
        panel, constraints, componentService.toolBars().feedbacks());
  }
}
