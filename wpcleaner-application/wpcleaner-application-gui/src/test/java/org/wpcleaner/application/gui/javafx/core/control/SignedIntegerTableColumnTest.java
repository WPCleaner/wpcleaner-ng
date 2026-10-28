package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

class SignedIntegerTableColumnTest extends JavaFxTest {

  @DisplayName(
      "SignedIntegerTableColumn formats positive, negative, zero, and null integers correctly")
  @Test
  void testSignedIntegerTableColumnFormatting()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final SignedIntegerTableColumn<Integer> column =
              new SignedIntegerTableColumn<>("Delta", val -> val);

          // Test positive value formatting (+5)
          final TableColumn.CellDataFeatures<Integer, String> featurePositive =
              new TableColumn.CellDataFeatures<>(null, column, 5);
          Assertions.assertThat(column.getCellValueFactory().call(featurePositive).getValue())
              .isEqualTo("+5");

          // Test negative value formatting (-10)
          final TableColumn.CellDataFeatures<Integer, String> featureNegative =
              new TableColumn.CellDataFeatures<>(null, column, -10);
          Assertions.assertThat(column.getCellValueFactory().call(featureNegative).getValue())
              .isEqualTo("-10");

          // Test zero formatting (+0)
          final TableColumn.CellDataFeatures<Integer, String> featureZero =
              new TableColumn.CellDataFeatures<>(null, column, 0);
          Assertions.assertThat(column.getCellValueFactory().call(featureZero).getValue())
              .isEqualTo("+0");

          // Test null value formatting (empty string)
          final TableColumn.CellDataFeatures<Integer, String> featureNull =
              new TableColumn.CellDataFeatures<>(null, column, null);
          Assertions.assertThat(column.getCellValueFactory().call(featureNull).getValue())
              .isEmpty();

          // Test cell factory and alignment
          final TableCell<Integer, String> cell = column.getCellFactory().call(column);
          Assertions.assertThat(cell).isNotNull();
          Assertions.assertThat(cell.getAlignment()).isEqualTo(Pos.TOP_RIGHT);
        });
  }
}
