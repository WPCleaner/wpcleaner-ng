package org.wpcleaner.application.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.jspecify.annotations.Nullable;

public final class FunctionListCellRenderer<T> implements ListCellRenderer<T> {

  private final ListCellRenderer<Object> renderer;
  private final Function<T, String> mapper;

  public FunctionListCellRenderer(final Function<T, String> mapper) {
    this.renderer = new DefaultListCellRenderer();
    this.mapper = mapper;
  }

  @Override
  public Component getListCellRendererComponent(
      final JList<? extends T> list,
      @Nullable final T value,
      final int index,
      final boolean isSelected,
      final boolean cellHasFocus) {
    final String text = value == null ? "" : mapper.apply(value);
    return renderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
  }
}
