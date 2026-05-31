package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serial;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;

final class ByPagePanel extends JPanel {

  @Serial private static final long serialVersionUID = 1L;

  private final transient SwingCoreServices swingCoreServices;
  private final transient WikiDefinition wiki;
  private final transient InterestingSettingsManager interestingSettings;

  ByPagePanel(final SwingMainWindowServices services) {
    super(new GridBagLayout());
    this.swingCoreServices = services.swing();
    this.wiki = services.user().getCurrentUser().wiki();
    this.interestingSettings = services.interestingSettings();
    initialize();
  }

  private void initialize() {
    final GridBagConstraints constraints = swingCoreServices.layout().initializeConstraints();
    final PageInput page = new PageInput(wiki, swingCoreServices, interestingSettings);
    addLine(this, constraints, page.label, page.icon, page.comboBox, page.toolBar);
    swingCoreServices.layout().addFillingPanelBelow(this);
  }

  private void addLine(
      final JPanel panel,
      final GridBagConstraints constraints,
      final JLabel label,
      final JLabel icon,
      final JComponent selector,
      final JToolBar toolBar) {
    swingCoreServices
        .layout()
        .addRow(
            panel,
            constraints,
            GridBagComponent.of(label),
            GridBagComponent.of(icon),
            GridBagComponent.of(selector, cellConstraints -> cellConstraints.weightx = 1),
            GridBagComponent.of(toolBar));
  }
}
