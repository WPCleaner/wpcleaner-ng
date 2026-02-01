package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;
import org.wpcleaner.application.gui.swing.core.layout.GridBagLayoutService;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindow;

public final class SwingMainWindow extends WPCleanerWindow {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final transient ConnectedUser user;

  public static void create(final SwingCoreServices swingCoreServices, final ConnectedUser user) {
    final SwingMainWindow window = new SwingMainWindow(swingCoreServices, user);
    window.initialize();
  }

  private SwingMainWindow(final SwingCoreServices swingCoreServices, final ConnectedUser user) {
    super(swingCoreServices);
    this.user = user;
  }

  @Override
  protected void initialize() {
    super.initialize();
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = swingCore.layout().initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    addWelcomeMessage(panel, constraints);
    addButtons(panel, constraints);

    swingCore.layout().addFillingPanelBelow(panel);
    getContentPane().add(panel);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void addWelcomeMessage(final JPanel panel, final GridBagConstraints constraints) {
    final GridBagLayoutService layout = swingCore.layout();
    layout.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Welcome %s on WPCleaner Next Generation!".formatted(user.username()),
                SwingConstants.CENTER)));
    layout.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "You are currently connected to %s".formatted(user.wiki()),
                SwingConstants.CENTER)));
    if (user.demo()) {
      layout.addRow(
          panel,
          constraints,
          GridBagComponent.of(
              new JLabel(
                  "You are currently in demo mode, you won't be able to save your modifications",
                  SwingConstants.CENTER)));
    }
    layout.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Your groups: %s".formatted(StringUtils.joinWithEllipsis(user.groups(), 3)),
                SwingConstants.CENTER)));
    layout.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            new JLabel(
                "Your rights: %s".formatted(StringUtils.joinWithEllipsis(user.rights(), 3)),
                SwingConstants.CENTER)));
  }

  private void addButtons(final JPanel panel, final GridBagConstraints constraints) {
    swingCore
        .layout()
        .addRowSpanningAllColumns(panel, constraints, swingCore.component().toolBars().feedbacks());
  }
}
