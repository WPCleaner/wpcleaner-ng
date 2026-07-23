package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.IconListStringTableColumn;
import org.wpcleaner.application.gui.javafx.core.TimeTableColumn;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesTableView extends TableView<RecentChange> {

  public RecentChangesTableView(
      final ObservableList<RecentChange> items, final JavaFxImageLoader imageLoader) {
    super(items);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    final TableColumn<RecentChange, @Nullable Instant> timeCol =
        new TimeTableColumn<>("Time", RecentChange::timestamp);

    final TableColumn<RecentChange, String> titleCol = new TableColumn<>("Title");
    titleCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(Objects.requireNonNullElse(cellData.getValue().title(), "")));
    titleCol.setPrefWidth(200);

    final TableColumn<RecentChange, String> userCol = new TableColumn<>("User");
    userCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(Objects.requireNonNullElse(cellData.getValue().user(), "")));
    userCol.setPrefWidth(120);
    userCol.setResizable(false);

    final TableColumn<RecentChange, String> commentCol = new TableColumn<>("Comment");
    commentCol.setCellValueFactory(
        cellData ->
            new SimpleStringProperty(
                Objects.requireNonNullElse(cellData.getValue().comment(), "")));
    commentCol.setPrefWidth(400);

    final Image tagIcon = imageLoader.getImage(ImageCollection.TAG, ImageSize.BUTTON).orElse(null);
    final TableColumn<RecentChange, @Nullable List<String>> tagsCol =
        new IconListStringTableColumn<>("Tags", tagIcon, RecentChange::tags);

    getColumns().add(timeCol);
    getColumns().add(titleCol);
    getColumns().add(userCol);
    getColumns().add(commentCol);
    getColumns().add(tagsCol);
  }
}
