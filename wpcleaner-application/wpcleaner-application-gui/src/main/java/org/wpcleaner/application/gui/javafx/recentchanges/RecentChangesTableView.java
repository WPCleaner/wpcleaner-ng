package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.control.IconListStringTableColumn;
import org.wpcleaner.application.gui.javafx.core.control.SignedIntegerTableColumn;
import org.wpcleaner.application.gui.javafx.core.control.TimeTableColumn;
import org.wpcleaner.application.gui.javafx.core.control.UrlTableColumn;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesTableView extends TableView<FilteredRecentChange> {

  public RecentChangesTableView(
      final ObservableList<FilteredRecentChange> items,
      final JavaFxImageLoader imageLoader,
      final JavaFxActionServices actionServices,
      final Consumer<FilteredRecentChange> viewAction) {
    super(items);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    final TableColumn<FilteredRecentChange, @Nullable Severity> severityCol =
        new SeverityTableColumn<>("", imageLoader, item -> item.filter().severity());

    final TableColumn<FilteredRecentChange, @Nullable Instant> timeCol =
        new TimeTableColumn<>(GT._T("Time"), FilteredRecentChange::timestamp);

    final TableColumn<FilteredRecentChange, String> titleCol = new TableColumn<>(GT._T("Title"));
    titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().title()));
    titleCol.setPrefWidth(200);

    final TableColumn<FilteredRecentChange, String> userCol = new TableColumn<>(GT._T("User"));
    userCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().user()));
    userCol.setPrefWidth(120);
    userCol.setResizable(false);

    final TableColumn<FilteredRecentChange, String> deltaCol =
        new SignedIntegerTableColumn<>("+/-", FilteredRecentChange::delta);

    final TableColumn<FilteredRecentChange, String> commentCol =
        new TableColumn<>(GT._T("Comment"));
    commentCol.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().comment()));
    commentCol.setPrefWidth(400);

    final Image tagIcon = imageLoader.getImage(ImageCollection.TAG, ImageSize.BUTTON).orElse(null);
    final TableColumn<FilteredRecentChange, List<String>> tagsCol =
        new IconListStringTableColumn<>(GT._T("Tags"), tagIcon, FilteredRecentChange::tags);

    final TableColumn<FilteredRecentChange, @Nullable URI> pageURICol =
        new UrlTableColumn<>(
            "",
            imageLoader,
            actionServices,
            ImageCollection.OPEN_URL,
            FilteredRecentChange::pageURI);

    final TableColumn<FilteredRecentChange, @Nullable URI> diffURICol =
        new UrlTableColumn<>(
            "", imageLoader, actionServices, ImageCollection.DIFF, FilteredRecentChange::diffURI);

    final TableColumn<FilteredRecentChange, FilteredRecentChange> viewCol =
        new ViewModificationTableColumn(imageLoader, viewAction);

    getColumns().add(severityCol);
    getColumns().add(timeCol);
    getColumns().add(titleCol);
    getColumns().add(userCol);
    getColumns().add(deltaCol);
    getColumns().add(commentCol);
    getColumns().add(tagsCol);
    getColumns().add(pageURICol);
    getColumns().add(diffURICol);
    getColumns().add(viewCol);
  }
}
