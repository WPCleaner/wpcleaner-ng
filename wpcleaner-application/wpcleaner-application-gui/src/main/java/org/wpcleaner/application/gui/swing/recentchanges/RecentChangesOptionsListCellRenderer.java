package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class RecentChangesOptionsListCellRenderer
    implements ListCellRenderer<RecentChangesOptions> {

  private final ListCellRenderer<Object> renderer;

  public RecentChangesOptionsListCellRenderer() {
    this.renderer = new DefaultListCellRenderer();
  }

  @Override
  public Component getListCellRendererComponent(
      final JList<? extends RecentChangesOptions> list,
      final RecentChangesOptions value,
      final int index,
      final boolean isSelected,
      final boolean cellHasFocus) {
    return renderer.getListCellRendererComponent(
        list, value.name(), index, isSelected, cellHasFocus);
  }
}
