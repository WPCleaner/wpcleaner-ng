package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;
import org.wpcleaner.application.gui.javafx.core.control.ImageToggleButton;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesListPanel extends VBox {

  private final Timeline timeline;

  public RecentChangesListPanel(
      final Window owner,
      final JavaFxRecentChangesWindowServices services,
      final JavaFxImageLoader imageLoader,
      final JavaFxProgressTracker progressTracker,
      final BooleanProperty loading,
      final Consumer<FilteredRecentChange> viewAction) {
    super(10);

    final RecentChangesOptionsInput optionsInput =
        new RecentChangesOptionsInput(owner, services, imageLoader);
    final ObservableList<FilteredRecentChange> tableItems = FXCollections.observableArrayList();

    final RecentChangesListRefresher refresher =
        new RecentChangesListRefresher(
            services, optionsInput, tableItems, progressTracker, loading);

    this.timeline =
        new Timeline(new KeyFrame(Duration.seconds(60), _ -> refresher.refreshList(false)));
    this.timeline.setCycleCount(Animation.INDEFINITE);

    final Image unselected =
        imageLoader.getImage(ImageCollection.REFRESH_STOP, ImageSize.BUTTON).orElse(null);
    final Image selected =
        imageLoader.getImage(ImageCollection.REFRESH, ImageSize.BUTTON).orElse(null);
    final ToggleButton refreshButton =
        new ImageToggleButton(GT._T("Refresh list"), unselected, selected);

    refreshButton
        .selectedProperty()
        .addListener(
            (_, _, isSelected) -> {
              if (isSelected) {
                refresher.refreshList(true);
                timeline.play();
              } else {
                timeline.stop();
              }
            });

    final ToolBar toolbar = new ToolBar();
    toolbar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 5px;");
    toolbar
        .getItems()
        .addAll(
            refreshButton,
            optionsInput.getComboBox(),
            optionsInput.getEditButton(),
            optionsInput.getAddButton(),
            optionsInput.getRemoveButton());

    final TableView<FilteredRecentChange> tableView =
        new RecentChangesTableView(tableItems, imageLoader, services.actionServices(), viewAction);
    setVgrow(tableView, Priority.ALWAYS);

    getChildren().addAll(toolbar, tableView);
    setVgrow(this, Priority.ALWAYS);
  }

  public void stop() {
    timeline.stop();
  }
}
