package org.wpcleaner.gui.swing.core.layout;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.JPanel;
import org.springframework.stereotype.Service;

@Service
public class GridBagLayoutService {

  public GridBagConstraints initializeConstraints() {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.insets = new Insets(1, 1, 1, 1);
    constraints.ipadx = 0;
    constraints.ipady = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;
    return constraints;
  }

  public int getColumnsCount(final JPanel panel) {
    if (!(panel.getLayout() instanceof GridBagLayout gridBagLayout)) {
      return 0;
    }
    return Arrays.stream(panel.getComponents())
        .map(gridBagLayout::getConstraints)
        .mapToInt(constraints -> constraints.gridx + constraints.gridwidth)
        .max()
        .orElse(0);
  }

  public int getRowsCount(final JPanel panel) {
    if (!(panel.getLayout() instanceof GridBagLayout gridBagLayout)) {
      return 0;
    }
    return Arrays.stream(panel.getComponents())
        .map(gridBagLayout::getConstraints)
        .mapToInt(constraints -> constraints.gridy + constraints.gridheight)
        .max()
        .orElse(0);
  }

  public void addFillingPanelBelow(final JPanel panel) {
    if (!(panel.getLayout() instanceof GridBagLayout)) {
      return;
    }
    final JPanel emptyPanel = new JPanel();
    emptyPanel.setMinimumSize(new Dimension(0, 0));
    emptyPanel.setPreferredSize(new Dimension(0, 0));
    final GridBagConstraints constraints = initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridwidth = getColumnsCount(panel);
    constraints.gridy = getRowsCount(panel);
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 1;
    constraints.weighty = 1;
    panel.add(emptyPanel, constraints);
  }
}
