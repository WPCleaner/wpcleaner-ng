package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JTable;
import org.jspecify.annotations.Nullable;

public class ColumnBuilder<C, T> {

  private final String title;
  private final Function<C, T> fieldExtractor;
  private Function<T, Object> fieldFormatter;
  private final List<BiConsumer<JTable, Integer>> configurers;

  public ColumnBuilder(final String title, final Function<C, @Nullable T> fieldExtractor) {
    this.title = title;
    this.fieldExtractor = fieldExtractor;
    this.fieldFormatter = value -> (Object) value;
    this.configurers = new ArrayList<>();
  }

  public ColumnBuilder<C, T> withFieldFormatter(final Function<T, Object> formatter) {
    this.fieldFormatter = formatter;
    return this;
  }

  public ColumnBuilder<C, T> withConfigurer(final BiConsumer<JTable, Integer> configurer) {
    this.configurers.add(configurer);
    return this;
  }

  public ColumnInformation<C, T> build() {
    return new ColumnInformation<>(title, fieldExtractor, fieldFormatter, configurers);
  }
}
