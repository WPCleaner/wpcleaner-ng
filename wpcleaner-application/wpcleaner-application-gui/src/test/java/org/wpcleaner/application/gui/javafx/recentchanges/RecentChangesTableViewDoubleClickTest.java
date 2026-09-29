package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
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
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;

class RecentChangesTableViewDoubleClickTest extends JavaFxTest {

  private static final int[] DOUBLE_CLICK_COLUMNS = {1, 2, 3, 4, 5};
  private static final int[] NON_DOUBLE_CLICK_COLUMNS = {0, 6, 7, 8};

  @FunctionalInterface
  private interface ViewAction extends Consumer<FilteredRecentChange> {}

  @DisplayName(
      "Double-clicking on cells in Time, Title, User, Delta, and Comment columns triggers viewAction")
  @Test
  void doubleClickTriggersViewAction()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);
          Mockito.when(mockImageLoader.getImageView(Mockito.any(), Mockito.any()))
              .thenReturn(Optional.empty());
          final JavaFxActionServices mockActionsServices = Mockito.mock(JavaFxActionServices.class);

          final ObservableList<FilteredRecentChange> items = FXCollections.observableArrayList();
          final FilteredRecentChange rc = Mockito.mock(FilteredRecentChange.class);
          items.add(rc);

          final ViewAction mockViewAction = Mockito.mock(ViewAction.class);

          final RecentChangesTableView tableView =
              new RecentChangesTableView(
                  items, mockImageLoader, mockActionsServices, mockViewAction);

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
            Mockito.verify(mockViewAction, Mockito.never()).accept(Mockito.any());

            cell.getOnMouseClicked().handle(rightDoubleClickEvent);
            Mockito.verify(mockViewAction, Mockito.never()).accept(Mockito.any());

            final TableCell<FilteredRecentChange, Object> emptyCell =
                col.getCellFactory().call(col);
            final TableRow<FilteredRecentChange> emptyRow = new TableRow<>();
            emptyRow.updateTableView(tableView);
            emptyRow.updateIndex(100);
            emptyCell.updateTableView(tableView);
            emptyCell.updateTableRow(emptyRow);
            emptyCell.updateIndex(100);
            emptyCell.getOnMouseClicked().handle(doubleClickEvent);
            Mockito.verify(mockViewAction, Mockito.never()).accept(Mockito.any());

            cell.getOnMouseClicked().handle(doubleClickEvent);
            Mockito.verify(mockViewAction).accept(rc);
            Mockito.clearInvocations(mockViewAction);
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
          final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);
          Mockito.when(mockImageLoader.getImageView(Mockito.any(), Mockito.any()))
              .thenReturn(Optional.empty());
          final JavaFxActionServices mockActionsServices = Mockito.mock(JavaFxActionServices.class);

          final ObservableList<FilteredRecentChange> items = FXCollections.observableArrayList();
          final FilteredRecentChange rc = Mockito.mock(FilteredRecentChange.class);
          items.add(rc);

          final ViewAction mockViewAction = Mockito.mock(ViewAction.class);

          final RecentChangesTableView tableView =
              new RecentChangesTableView(
                  items, mockImageLoader, mockActionsServices, mockViewAction);

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
          Mockito.verify(mockViewAction).accept(rc);
        });
  }
}
