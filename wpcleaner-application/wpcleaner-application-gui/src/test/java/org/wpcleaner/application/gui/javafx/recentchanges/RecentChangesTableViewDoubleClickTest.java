package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;

@GuiSpringBootTest
class RecentChangesTableViewDoubleClickTest extends JavaFxTest {

  private static final FilteredRecentChange RECENT_CHANGE =
      FilteredRecentChangeFixtures.create(0, "Main Page");

  private static final int[] DOUBLE_CLICK_COLUMNS = {1, 2, 3, 4, 5};
  private static final int[] NON_DOUBLE_CLICK_COLUMNS = {0, 6, 7, 8};

  @Autowired private JavaFxRecentChangesWindowServices services;

  @DisplayName(
      "Double-clicking on cells in Time, Title, User, Delta, and Comment columns triggers viewAction")
  @Test
  void doubleClickTriggersViewAction()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ObservableList<FilteredRecentChange> items =
              FXCollections.observableArrayList(RECENT_CHANGE);
          final List<FilteredRecentChange> viewed = new ArrayList<>();
          final RecentChangesTableView tableView =
              new RecentChangesTableView(
                  items,
                  new JavaFxImageLoader(services.imageLoader()),
                  services.actionServices(),
                  viewed::add);

          final MouseEvent doubleClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.PRIMARY,
                  2,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  null);

          final MouseEvent singleClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.PRIMARY,
                  1,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  null);

          final MouseEvent rightDoubleClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.SECONDARY,
                  2,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  null);

          for (final int columnIndex : DOUBLE_CLICK_COLUMNS) {
            @SuppressWarnings("unchecked")
            final TableColumn<FilteredRecentChange, Object> col =
                (TableColumn<FilteredRecentChange, Object>) tableView.getColumns().get(columnIndex);

            final TableCell<FilteredRecentChange, Object> cell = col.getCellFactory().call(col);
            Assertions.assertThat(cell.getOnMouseClicked()).isNotNull();

            final TableRow<FilteredRecentChange> row = new TableRow<>();
            row.updateTableView(tableView);
            row.updateIndex(0);
            cell.updateTableView(tableView);
            cell.updateTableRow(row);
            cell.updateIndex(0);

            cell.getOnMouseClicked().handle(singleClickEvent);
            Assertions.assertThat(viewed).isEmpty();

            cell.getOnMouseClicked().handle(rightDoubleClickEvent);
            Assertions.assertThat(viewed).isEmpty();

            final TableCell<FilteredRecentChange, Object> emptyCell =
                col.getCellFactory().call(col);
            final TableRow<FilteredRecentChange> emptyRow = new TableRow<>();
            emptyRow.updateTableView(tableView);
            emptyRow.updateIndex(100);
            emptyCell.updateTableView(tableView);
            emptyCell.updateTableRow(emptyRow);
            emptyCell.updateIndex(100);
            emptyCell.getOnMouseClicked().handle(doubleClickEvent);
            Assertions.assertThat(viewed).isEmpty();

            cell.getOnMouseClicked().handle(doubleClickEvent);
            Assertions.assertThat(viewed).containsExactly(RECENT_CHANGE);
            viewed.clear();
          }

          for (final int columnIndex : NON_DOUBLE_CLICK_COLUMNS) {
            @SuppressWarnings("unchecked")
            final TableColumn<FilteredRecentChange, Object> col =
                (TableColumn<FilteredRecentChange, Object>) tableView.getColumns().get(columnIndex);
            final TableCell<FilteredRecentChange, Object> cell = col.getCellFactory().call(col);
            Assertions.assertThat(cell.getOnMouseClicked()).isNull();
          }
        });
  }

  @DisplayName("Double-clicking resolves item from TableView when TableRow is null")
  @Test
  void doubleClickResolvesItemFromTableViewWhenTableRowIsNull()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final ObservableList<FilteredRecentChange> items =
              FXCollections.observableArrayList(RECENT_CHANGE);
          final List<FilteredRecentChange> viewed = new ArrayList<>();
          final RecentChangesTableView tableView =
              new RecentChangesTableView(
                  items,
                  new JavaFxImageLoader(services.imageLoader()),
                  services.actionServices(),
                  viewed::add);

          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, String> titleCol =
              (TableColumn<FilteredRecentChange, String>) tableView.getColumns().get(2);

          final TableCell<FilteredRecentChange, String> cell =
              titleCol.getCellFactory().call(titleCol);
          cell.updateTableView(tableView);
          cell.updateIndex(0);

          final MouseEvent doubleClickEvent =
              new MouseEvent(
                  MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.PRIMARY,
                  2,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  false,
                  null);

          cell.getOnMouseClicked().handle(doubleClickEvent);
          Assertions.assertThat(viewed).containsExactly(RECENT_CHANGE);
        });
  }
}
