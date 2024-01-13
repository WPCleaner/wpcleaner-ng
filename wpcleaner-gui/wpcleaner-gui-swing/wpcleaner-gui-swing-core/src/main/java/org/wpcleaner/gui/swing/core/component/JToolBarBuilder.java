package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class JToolBarBuilder {

  private final List<Component> components = new ArrayList<>();

  public JToolBarBuilder withComponent(final Component component) {
    components.add(component);
    return this;
  }

  public JToolBarBuilder withSeparator() {
    components.add(new JToolBar.Separator());
    return this;
  }

  public JToolBar build() {
    final JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
    toolBar.setFloatable(false);
    toolBar.setBorderPainted(false);
    components.forEach(toolBar::add);
    return toolBar;
  }
}
