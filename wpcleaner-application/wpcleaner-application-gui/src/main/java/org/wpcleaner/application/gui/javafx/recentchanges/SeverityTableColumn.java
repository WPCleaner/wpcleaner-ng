package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;

public final class SeverityTableColumn<S> extends TableColumn<S, @Nullable Severity> {

  public SeverityTableColumn(
      final String title,
      final JavaFxImageLoader imageLoader,
      final Function<S, @Nullable Severity> mapper) {
    super(title);
    setCellValueFactory(cellData -> new SimpleObjectProperty<>(mapper.apply(cellData.getValue())));
    setCellFactory(_ -> new SeverityTableCell<>(imageLoader));
    setPrefWidth(30);
    setResizable(false);
  }
}
