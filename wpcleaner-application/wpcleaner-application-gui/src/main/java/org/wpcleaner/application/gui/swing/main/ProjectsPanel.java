package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;

final class ProjectsPanel extends JPanel {

  @Serial private static final long serialVersionUID = 1L;

  private final transient SwingMainWindowServices services;

  ProjectsPanel(final SwingMainWindowServices services) {
    super(new GridBagLayout());
    this.services = services;
    initialize();
  }

  private void initialize() {
    final GridBagConstraints constraints = services.swing().layout().initializeConstraints();

    final JButton button =
        services
            .swing()
            .component()
            .buttons()
            .builder("Recent changes", true)
            .withAction(_ -> services.recentChangesWindowFactory().displayRecentChangesWindow())
            .build();

    services.swing().layout().addRow(this, constraints, GridBagComponent.of(button));
    services.swing().layout().addFillingPanelBelow(this);
  }
}
