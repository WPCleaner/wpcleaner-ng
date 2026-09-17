package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

final class TextAreaTableCell<S> extends TableCell<S, String> {

  private final TextArea cellTextArea = new TextArea();

  public TextAreaTableCell() {
    super();
    cellTextArea.setPrefRowCount(3);
    cellTextArea.setWrapText(true);

    cellTextArea
        .focusedProperty()
        .addListener(
            (_, _, newVal) -> {
              if (!newVal) {
                commitEdit(cellTextArea.getText());
              }
            });

    cellTextArea.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.ESCAPE) {
            cancelEdit();
          }
        });
  }

  @Override
  public void startEdit() {
    if (!isEmpty()) {
      super.startEdit();
      cellTextArea.setText(getItem());
      setText(null);
      setGraphic(cellTextArea);
      cellTextArea.selectAll();
      cellTextArea.requestFocus();
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setText(getItem());
    setGraphic(null);
  }

  @Override
  protected void updateItem(final String item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        cellTextArea.setText(item);
        setText(null);
        setGraphic(cellTextArea);
      } else {
        setText(item);
        setGraphic(null);
      }
    }
  }
}
