package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.time.Instant;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.IconListStringTableColumn;
import org.wpcleaner.application.gui.javafx.core.SignedIntegerTableColumn;
import org.wpcleaner.application.gui.javafx.core.TimeTableColumn;
import org.wpcleaner.application.gui.javafx.core.UrlTableColumn;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesTableView extends TableView<FilteredRecentChange> {

  public RecentChangesTableView(
      final ObservableList<FilteredRecentChange> items,
      final JavaFxImageLoader imageLoader,
      final DesktopService desktopService) {
    super(items);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    final TableColumn<FilteredRecentChange, @Nullable Severity> severityCol =
        new SeverityTableColumn<>("", imageLoader, item -> item.filter().severity());

    final TableColumn<FilteredRecentChange, @Nullable Instant> timeCol =
        new TimeTableColumn<>("Time", FilteredRecentChange::timestamp);

    final TableColumn<FilteredRecentChange, String> titleCol = new TableColumn<>("Title");
    titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().title()));
    titleCol.setPrefWidth(200);

    final TableColumn<FilteredRecentChange, String> userCol = new TableColumn<>("User");
    userCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().user()));
    userCol.setPrefWidth(120);
    userCol.setResizable(false);

    final TableColumn<FilteredRecentChange, String> deltaCol =
        new SignedIntegerTableColumn<>("+/-", FilteredRecentChange::delta);

    final TableColumn<FilteredRecentChange, String> commentCol = new TableColumn<>("Comment");
    commentCol.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().comment()));
    commentCol.setPrefWidth(400);

    final Image tagIcon = imageLoader.getImage(ImageCollection.TAG, ImageSize.BUTTON).orElse(null);
    final TableColumn<FilteredRecentChange, List<String>> tagsCol =
        new IconListStringTableColumn<>("Tags", tagIcon, FilteredRecentChange::tags);

    final TableColumn<FilteredRecentChange, @Nullable URI> pageURICol =
        new UrlTableColumn<>(
            "",
            imageLoader,
            desktopService,
            ImageCollection.OPEN_URL,
            FilteredRecentChange::pageURI);

    final TableColumn<FilteredRecentChange, @Nullable URI> diffURICol =
        new UrlTableColumn<>(
            "", imageLoader, desktopService, ImageCollection.DIFF, FilteredRecentChange::diffURI);

    getColumns().add(severityCol);
    getColumns().add(timeCol);
    getColumns().add(titleCol);
    getColumns().add(userCol);
    getColumns().add(deltaCol);
    getColumns().add(commentCol);
    getColumns().add(tagsCol);
    getColumns().add(pageURICol);
    getColumns().add(diffURICol);
  }
}
