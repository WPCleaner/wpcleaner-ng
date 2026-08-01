package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.control.UrlTableCell;
import org.wpcleaner.application.gui.javafx.core.control.UrlTableColumn;

class RecentChangesTableViewTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runOnJavaFx(final Runnable runnable)
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (final AssertionError | Exception e) {
            future.completeExceptionally(e);
          }
        });
    future.get(5, TimeUnit.SECONDS);
  }

  @DisplayName("RecentChangesTableView initializes columns and behaves correctly")
  @Test
  void testRecentChangesTableViewInitialization()
      throws InterruptedException,
          java.util.concurrent.ExecutionException,
          java.util.concurrent.TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxImageLoader mockImageLoader = Mockito.mock(JavaFxImageLoader.class);
          Mockito.when(mockImageLoader.getImageView(Mockito.any(), Mockito.any()))
              .thenReturn(Optional.empty());

          final JavaFxActionServices mockActionsServices = Mockito.mock(JavaFxActionServices.class);

          final ObservableList<FilteredRecentChange> items = FXCollections.observableArrayList();
          final FilteredRecentChange rc = Mockito.mock(FilteredRecentChange.class);
          Mockito.when(rc.diffURI())
              .thenReturn(URI.create("https://en.wikipedia.org/wiki/Special:Diff/12345"));
          Mockito.when(rc.pageURI())
              .thenReturn(URI.create("https://en.wikipedia.org/wiki/Main_Page%3FTest"));
          Mockito.when(rc.title()).thenReturn("Main Page?Test");

          final RecentChangesFilter mockFilter = Mockito.mock(RecentChangesFilter.class);
          Mockito.when(mockFilter.severity()).thenReturn(Severity.ALERT_4);
          Mockito.when(rc.filter()).thenReturn(mockFilter);
          items.add(rc);

          final RecentChangesTableView tableView =
              new RecentChangesTableView(items, mockImageLoader, mockActionsServices, _ -> {});

          // Verify total columns size
          Assertions.assertThat(tableView.getColumns()).hasSize(10);

          // Verify severityCol at index 0
          final TableColumn<FilteredRecentChange, ?> severityCol = tableView.getColumns().get(0);
          Assertions.assertThat(severityCol.getText()).isEmpty();
          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, Severity> castedSeverityCol =
              (TableColumn<FilteredRecentChange, Severity>) severityCol;

          final TableColumn.CellDataFeatures<FilteredRecentChange, Severity> severityFeature =
              new TableColumn.CellDataFeatures<>(tableView, castedSeverityCol, rc);
          final Severity severityValue =
              castedSeverityCol.getCellValueFactory().call(severityFeature).getValue();
          Assertions.assertThat(severityValue).isEqualTo(Severity.ALERT_4);

          // Verify timeCol, titleCol, and pageURICol
          final TableColumn<FilteredRecentChange, ?> pageURICol = tableView.getColumns().get(7);
          Assertions.assertThat(pageURICol).isInstanceOf(UrlTableColumn.class);
          Assertions.assertThat(pageURICol.getText()).isEmpty();
          // Verify cell value factory for page
          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, URI> castedPageURICol =
              (TableColumn<FilteredRecentChange, URI>) pageURICol;

          final TableColumn.CellDataFeatures<FilteredRecentChange, URI> pageURIFeature =
              new TableColumn.CellDataFeatures<>(tableView, castedPageURICol, rc);
          final URI pageURIValue =
              castedPageURICol.getCellValueFactory().call(pageURIFeature).getValue();
          final URI expectedPageUri = URI.create("https://en.wikipedia.org/wiki/Main_Page%3FTest");
          Assertions.assertThat(pageURIValue).isEqualTo(expectedPageUri);

          // Verify diffURICol at index 8
          final TableColumn<FilteredRecentChange, ?> diffURICol = tableView.getColumns().get(8);
          Assertions.assertThat(diffURICol).isInstanceOf(UrlTableColumn.class);
          Assertions.assertThat(diffURICol.getText()).isEmpty();

          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, URI> castedDiffURICol =
              (TableColumn<FilteredRecentChange, URI>) diffURICol;

          final TableColumn.CellDataFeatures<FilteredRecentChange, URI> diffURIFeature =
              new TableColumn.CellDataFeatures<>(tableView, castedDiffURICol, rc);
          final URI diffURIValue =
              castedDiffURICol.getCellValueFactory().call(diffURIFeature).getValue();
          final URI expectedDiffUri =
              URI.create("https://en.wikipedia.org/wiki/Special:Diff/12345");
          Assertions.assertThat(diffURIValue).isEqualTo(expectedDiffUri);

          // Verify viewCol at index 9
          final TableColumn<FilteredRecentChange, ?> viewCol = tableView.getColumns().get(9);
          Assertions.assertThat(viewCol).isInstanceOf(ViewModificationTableColumn.class);
          Assertions.assertThat(viewCol.getText()).isEmpty();

          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, FilteredRecentChange> castedViewCol =
              (TableColumn<FilteredRecentChange, FilteredRecentChange>) viewCol;

          final TableColumn.CellDataFeatures<FilteredRecentChange, FilteredRecentChange>
              viewFeature = new TableColumn.CellDataFeatures<>(tableView, castedViewCol, rc);
          final FilteredRecentChange viewValue =
              castedViewCol.getCellValueFactory().call(viewFeature).getValue();
          Assertions.assertThat(viewValue).isEqualTo(rc);

          // Verify cell factory and cell update
          final UrlTableCell<FilteredRecentChange> cell =
              (UrlTableCell<FilteredRecentChange>)
                  castedPageURICol.getCellFactory().call(castedPageURICol);
          Assertions.assertThat(cell).isNotNull();
          Assertions.assertThat(cell.getAlignment().name()).isEqualTo("CENTER");

          // Update item to a mock url and verify button is set as graphic
          cell.updateItem(expectedPageUri, false);
          Assertions.assertThat(cell.getGraphic()).isInstanceOf(Button.class);

          // Trigger button action and verify desktopService.browse is called
          final Button button = (Button) cell.getGraphic();
          button.getOnAction().handle(null);
          Mockito.verify(mockActionsServices, Mockito.timeout(1000))
              .browse("https://en.wikipedia.org/wiki/Main_Page%3FTest");

          // Verify SeverityTableCell factory and cell update
          final SeverityTableCell<FilteredRecentChange> severityCell =
              (SeverityTableCell<FilteredRecentChange>)
                  castedSeverityCol.getCellFactory().call(castedSeverityCol);
          Assertions.assertThat(severityCell).isNotNull();
          Assertions.assertThat(severityCell.getAlignment().name()).isEqualTo("CENTER");

          severityCell.updateItem(Severity.ALERT_4, false);
          Assertions.assertThat(severityCell.getTooltip())
              .isNull(); // because getImageView returns Optional.empty()
        });
  }
}
