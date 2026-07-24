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
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.UrlTableCell;
import org.wpcleaner.application.gui.javafx.core.UrlTableColumn;

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

          final WikiDefinition mockWiki = Mockito.mock(WikiDefinition.class);
          Mockito.when(mockWiki.mainHost()).thenReturn("en.wikipedia.org");
          Mockito.when(mockWiki.wikiPath()).thenReturn("/wiki");

          final DesktopService mockDesktopService = Mockito.mock(DesktopService.class);

          final ObservableList<RecentChange> items = FXCollections.observableArrayList();
          final RecentChange rc = Mockito.mock(RecentChange.class);
          Mockito.when(rc.title()).thenReturn("Main Page?Test");
          Mockito.when(rc.revid()).thenReturn(12_345);
          Mockito.when(rc.type()).thenReturn("edit");
          items.add(rc);

          final RecentChangesTableView tableView =
              new RecentChangesTableView(items, mockImageLoader, mockWiki, mockDesktopService);

          // Verify total columns size
          Assertions.assertThat(tableView.getColumns()).hasSize(8);

          // Verify timeCol, titleCol, and urlCol
          final TableColumn<RecentChange, ?> urlCol = tableView.getColumns().get(6);
          Assertions.assertThat(urlCol).isInstanceOf(UrlTableColumn.class);
          Assertions.assertThat(urlCol.getText()).isEmpty();
          // Verify cell value factory for urlCol
          @SuppressWarnings("unchecked")
          final TableColumn<RecentChange, URI> castedUrlCol =
              (TableColumn<RecentChange, URI>) urlCol;

          final TableColumn.CellDataFeatures<RecentChange, URI> feature =
              new TableColumn.CellDataFeatures<>(tableView, castedUrlCol, rc);
          final URI urlValue = castedUrlCol.getCellValueFactory().call(feature).getValue();
          final URI expectedUri = URI.create("https://en.wikipedia.org/wiki/Main_Page%3FTest");
          Assertions.assertThat(urlValue).isEqualTo(expectedUri);

          // Verify diffCol at index 7
          final TableColumn<RecentChange, ?> diffCol = tableView.getColumns().get(7);
          Assertions.assertThat(diffCol).isInstanceOf(UrlTableColumn.class);
          Assertions.assertThat(diffCol.getText()).isEmpty();

          @SuppressWarnings("unchecked")
          final TableColumn<RecentChange, URI> castedDiffCol =
              (TableColumn<RecentChange, URI>) diffCol;

          final TableColumn.CellDataFeatures<RecentChange, URI> diffFeature =
              new TableColumn.CellDataFeatures<>(tableView, castedDiffCol, rc);
          final URI diffValue = castedDiffCol.getCellValueFactory().call(diffFeature).getValue();
          final URI expectedDiffUri =
              URI.create("https://en.wikipedia.org/wiki/Special:Diff/12345");
          Assertions.assertThat(diffValue).isEqualTo(expectedDiffUri);

          // Verify cell factory and cell update
          final UrlTableCell<RecentChange> cell =
              (UrlTableCell<RecentChange>) castedUrlCol.getCellFactory().call(castedUrlCol);
          Assertions.assertThat(cell).isNotNull();
          Assertions.assertThat(cell.getAlignment().name()).isEqualTo("CENTER");

          // Update item to a mock url and verify button is set as graphic
          cell.updateItem(expectedUri, false);
          Assertions.assertThat(cell.getGraphic()).isInstanceOf(Button.class);

          // Trigger button action and verify desktopService.browse is called
          final Button button = (Button) cell.getGraphic();
          button.getOnAction().handle(null);
          Mockito.verify(mockDesktopService, Mockito.timeout(1000))
              .browse("https://en.wikipedia.org/wiki/Main_Page%3FTest");
        });
  }
}
