package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.JTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.api.utils.AutoCatch;

public record ColumnInformation<C, T>(
    String title,
    Function<C, T> extractor,
    Function<T, Object> formatter,
    List<BiConsumer<JTable, Integer>> configurers) {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public Object format(final C value) {
    final T field = extractor.apply(value);
    return AutoCatch.runOrDefault(
        () -> Objects.requireNonNullElse(formatter.apply(field), ""),
        "",
        e ->
            LOGGER.error(
                "Formatting value of type {} ({}) failed",
                field != null ? field.getClass().getSimpleName() : null,
                field,
                e));
  }
}
