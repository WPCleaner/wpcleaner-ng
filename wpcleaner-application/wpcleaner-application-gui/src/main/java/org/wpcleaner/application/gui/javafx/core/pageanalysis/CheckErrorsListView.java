package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Consumer;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.Nullable;

public final class CheckErrorsListView extends ListView<CheckAlgorithmErrors> {

  private static final int DOUBLE_CLICK_COUNT = 2;

  public CheckErrorsListView(final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    super();
    setCellFactory(_ -> createCell(doubleClickAction));
  }

  private static ListCell<CheckAlgorithmErrors> createCell(
      final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    final ListCell<CheckAlgorithmErrors> cell = new CheckAlgorithmErrorsCell();
    cell.setOnMouseClicked(event -> handleCellMouseClicked(event, cell, doubleClickAction));
    return cell;
  }

  private static void handleCellMouseClicked(
      final MouseEvent event,
      final ListCell<CheckAlgorithmErrors> cell,
      final Consumer<CheckAlgorithmErrors> doubleClickAction) {
    final CheckAlgorithmErrors item = cell.getItem();
    if (event.getButton() == MouseButton.PRIMARY
        && event.getClickCount() == DOUBLE_CLICK_COUNT
        && item != null) {
      doubleClickAction.accept(item);
    }
  }

  private static final class CheckAlgorithmErrorsCell extends ListCell<CheckAlgorithmErrors> {

    @Override
    protected void updateItem(@Nullable final CheckAlgorithmErrors item, final boolean empty) {
      super.updateItem(item, empty);
      setText(empty || item == null ? null : createText(item));
    }

    private static String createText(final CheckAlgorithmErrors item) {
      final String name = item.algorithm().name();
      final int errorsCount = item.errors().size();
      return errorsCount > 1 ? "%s (%d)".formatted(name, errorsCount) : name;
    }
  }
}
