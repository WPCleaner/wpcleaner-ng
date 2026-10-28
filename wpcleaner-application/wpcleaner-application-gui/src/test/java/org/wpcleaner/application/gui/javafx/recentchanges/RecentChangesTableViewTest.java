package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.control.UrlTableCell;
import org.wpcleaner.application.gui.javafx.core.control.UrlTableColumn;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;
import org.wpcleaner.application.gui.javafx.recentchanges.options.Severity;

@GuiSpringBootTest
class RecentChangesTableViewTest extends JavaFxTest {

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private DesktopService desktopService;

  @DisplayName("RecentChangesTableView initializes columns and behaves correctly")
  @Test
  void recentChangesTableViewInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    final FilteredRecentChange rc =
        new FilteredRecentChange(
            "comment",
            0,
            URI.create("https://en.wikipedia.org/wiki/Special:Diff/12345"),
            new RecentChangesFilter(
                "Alert", Set.of(), Severity.ALERT_4, Set.of(), Set.of(), Set.of(), null),
            0,
            URI.create("https://en.wikipedia.org/wiki/Main_Page%3FTest"),
            1,
            2,
            3,
            List.of(),
            null,
            "Main Page?Test",
            "User");

    runOnJavaFx(
        () -> {
          final ObservableList<FilteredRecentChange> items = FXCollections.observableArrayList(rc);
          final RecentChangesTableView tableView =
              new RecentChangesTableView(
                  items,
                  new JavaFxImageLoader(services.imageLoader()),
                  services.actionServices(),
                  _ -> {});

          Assertions.assertThat(tableView.getColumns()).hasSize(10);

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

          final TableColumn<FilteredRecentChange, ?> pageURICol = tableView.getColumns().get(7);
          Assertions.assertThat(pageURICol).isInstanceOf(UrlTableColumn.class);
          Assertions.assertThat(pageURICol.getText()).isEmpty();
          @SuppressWarnings("unchecked")
          final TableColumn<FilteredRecentChange, URI> castedPageURICol =
              (TableColumn<FilteredRecentChange, URI>) pageURICol;

          final TableColumn.CellDataFeatures<FilteredRecentChange, URI> pageURIFeature =
              new TableColumn.CellDataFeatures<>(tableView, castedPageURICol, rc);
          final URI pageURIValue =
              castedPageURICol.getCellValueFactory().call(pageURIFeature).getValue();
          final URI expectedPageUri = URI.create("https://en.wikipedia.org/wiki/Main_Page%3FTest");
          Assertions.assertThat(pageURIValue).isEqualTo(expectedPageUri);

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

          final UrlTableCell<FilteredRecentChange> cell =
              (UrlTableCell<FilteredRecentChange>)
                  castedPageURICol.getCellFactory().call(castedPageURICol);
          Assertions.assertThat(cell).isNotNull();
          Assertions.assertThat(cell.getAlignment().name()).isEqualTo("CENTER");

          cell.updateItem(expectedPageUri, false);
          Assertions.assertThat(cell.getGraphic()).isInstanceOf(Button.class);

          final Button button = (Button) cell.getGraphic();
          Assertions.assertThat(button.getTooltip()).isNotNull();
          Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Open page in browser");

          button.getOnAction().handle(null);

          final SeverityTableCell<FilteredRecentChange> severityCell =
              (SeverityTableCell<FilteredRecentChange>)
                  castedSeverityCol.getCellFactory().call(castedSeverityCol);
          Assertions.assertThat(severityCell).isNotNull();
          Assertions.assertThat(severityCell.getAlignment().name()).isEqualTo("CENTER");

          severityCell.updateItem(Severity.ALERT_4, false);
          Assertions.assertThat(severityCell.getGraphic()).isInstanceOf(ImageView.class);
          Assertions.assertThat(severityCell.getTooltip().getText())
              .isEqualTo(
                  Severity.ALERT_4.getName()); // because getImageView returns Optional.empty()
        });

    BDDMockito.then(desktopService)
        .should(BDDMockito.timeout(5000))
        .browse("https://en.wikipedia.org/wiki/Main_Page%3FTest");
  }
}
