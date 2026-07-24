package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jspecify.annotations.Nullable;

public class SignedIntegerTableColumn<S> extends TableColumn<S, String> {

  public SignedIntegerTableColumn(final String title, final Function<S, @Nullable Integer> mapper) {
    super(title);
    setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                Optional.ofNullable(mapper.apply(cellData.getValue()))
                    .map("%+d"::formatted)
                    .orElse("")));
    setCellFactory(
        _ -> {
          final TableCell<S, String> tc = new TextFieldTableCell<>();
          tc.setAlignment(Pos.TOP_RIGHT);
          return tc;
        });
    setPrefWidth(60);
    setResizable(false);
  }
}
