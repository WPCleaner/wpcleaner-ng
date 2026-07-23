package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jspecify.annotations.Nullable;

public class TimeTableColumn<S> extends TableColumn<S, @Nullable Instant> {

  public TimeTableColumn(final String title, final Function<S, @Nullable Instant> mapper) {
    super(title);
    setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));
    setCellFactory(_ -> new TimeTableCell<>());
    setPrefWidth(100);
    setResizable(false);
  }
}
