package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Consumer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;

public final class ViewModificationTableColumn
    extends TableColumn<FilteredRecentChange, FilteredRecentChange> {

  public ViewModificationTableColumn(
      final JavaFxImageLoader imageLoader, final Consumer<FilteredRecentChange> viewAction) {
    super("");
    setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    setCellFactory(_ -> new ViewModificationTableCell(imageLoader, viewAction));
    setPrefWidth(30);
    setResizable(false);
  }
}
