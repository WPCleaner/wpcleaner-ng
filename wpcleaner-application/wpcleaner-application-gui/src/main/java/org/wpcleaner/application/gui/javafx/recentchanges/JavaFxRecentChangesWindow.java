package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangeComparator;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.ImageToggleButton;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class JavaFxRecentChangesWindow extends Stage {

  private static final int MAX_RECENT_CHANGES = 1000;
  private static final Duration RECENT_CHANGES_OVERLAP = Duration.ofSeconds(10);
  private static final RecentChangesQuery DEFAULT_QUERY =
      RecentChangesQuery.emptyBuilder()
          .limit("max")
          .properties(
              Set.of(
                  RecentChangesParameters.Properties.COMMENT,
                  RecentChangesParameters.Properties.IDS,
                  RecentChangesParameters.Properties.TAGS,
                  RecentChangesParameters.Properties.TIMESTAMP,
                  RecentChangesParameters.Properties.TITLE,
                  RecentChangesParameters.Properties.USER))
          .build();

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final ObservableList<RecentChange> tableItems;
  private final RecentChangesOptionsInput optionsInput;
  private final Timeline timeline;
  @Nullable private Instant lastRecentChange;

  public JavaFxRecentChangesWindow(final JavaFxRecentChangesWindowServices services) {
    super();
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.tableItems = FXCollections.observableArrayList();
    this.optionsInput = new RecentChangesOptionsInput(this, services, imageLoader);
    this.timeline =
        new Timeline(new KeyFrame(javafx.util.Duration.seconds(60), _ -> refreshList()));
    this.timeline.setCycleCount(Animation.INDEFINITE);
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner - Recent changes");
    imageLoader.setWindowIcon(this);

    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));

    final Image unselected =
        imageLoader.getImage(ImageCollection.REFRESH_STOP, ImageSize.BUTTON).orElse(null);
    final Image selected =
        imageLoader.getImage(ImageCollection.REFRESH, ImageSize.BUTTON).orElse(null);
    final ToggleButton refreshButton = new ImageToggleButton("Refresh list", unselected, selected);

    refreshButton
        .selectedProperty()
        .addListener(
            (_, _, isSelected) -> {
              if (isSelected) {
                refreshList();
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

    final TableView<RecentChange> tableView = new RecentChangesTableView(tableItems, imageLoader);
    VBox.setVgrow(tableView, Priority.ALWAYS);

    mainContainer.getChildren().addAll(toolbar, tableView);

    final Scene scene = new Scene(mainContainer, 1200, 600);
    setScene(scene);

    setOnCloseRequest(_ -> timeline.stop());
  }

  private void refreshList() {
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final RecentChangesOptions currentOptions = optionsInput.getSelectedOptions();
    final RecentChangesQuery query =
        DEFAULT_QUERY
            .builder()
            .end(lastRecentChange)
            .namespace(currentOptions.namespace())
            .show(currentOptions.show())
            .tag(currentOptions.tag())
            .topOnly(currentOptions.topOnly())
            .type(currentOptions.type())
            .build();
    final List<RecentChange> recentChanges =
        services.apiRecentChanges().retrieveRecentChanges(wiki, query);
    recentChanges.sort(RecentChangeComparator.INSTANCE);
    int currentRowIndex = 0;
    for (final RecentChange rc : recentChanges) {
      while (currentRowIndex < tableItems.size()
          && RecentChangeComparator.INSTANCE.compare(rc, tableItems.get(currentRowIndex)) > 0) {
        currentRowIndex++;
      }
      if (currentRowIndex >= tableItems.size()
          || !Objects.equals(rc, tableItems.get(currentRowIndex))) {
        tableItems.add(currentRowIndex, rc);
        currentRowIndex++;
      }
    }
    while (tableItems.size() > MAX_RECENT_CHANGES) {
      tableItems.removeLast();
    }
    if (!recentChanges.isEmpty()) {
      lastRecentChange =
          Optional.ofNullable(recentChanges.getFirst().timestamp())
              .map(instant -> instant.minus(RECENT_CHANGES_OVERLAP))
              .orElse(null);
    }
  }
}
