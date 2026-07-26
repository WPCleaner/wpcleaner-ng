package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;
import org.wpcleaner.application.gui.javafx.core.control.ImageToggleButton;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

@SuppressWarnings("PMD.CouplingBetweenObjects")
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
                  RecentChangesParameters.Properties.SIZES,
                  RecentChangesParameters.Properties.TAGS,
                  RecentChangesParameters.Properties.TIMESTAMP,
                  RecentChangesParameters.Properties.TITLE,
                  RecentChangesParameters.Properties.USER))
          .build();

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final ObservableList<FilteredRecentChange> tableItems;
  private final RecentChangesOptionsInput optionsInput;
  private final Timeline timeline;
  private final BooleanProperty loading;
  private final JavaFxProgressTracker progressTracker;
  @Nullable private Instant lastRecentChange;

  public JavaFxRecentChangesWindow(final JavaFxRecentChangesWindowServices services) {
    super();
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.tableItems = FXCollections.observableArrayList();
    this.optionsInput = new RecentChangesOptionsInput(this, services, imageLoader);
    this.loading = new SimpleBooleanProperty(false);
    this.progressTracker = JavaFxProgressTracker.forObservable(loading);
    this.timeline =
        new Timeline(new KeyFrame(javafx.util.Duration.seconds(60), _ -> refreshList(false)));
    this.timeline.setCycleCount(Animation.INDEFINITE);
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner - " + GT._T("Recent changes"));
    imageLoader.setWindowIcon(this);
    services.windowsRegistry().register(this);

    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));

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
                refreshList(true);
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
        new RecentChangesTableView(tableItems, imageLoader, services.actionServices());
    VBox.setVgrow(tableView, Priority.ALWAYS);

    mainContainer.getChildren().addAll(toolbar, tableView);

    mainContainer.disableProperty().bind(loading);

    final StackPane root = new StackPane();
    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());

    final Scene scene = new Scene(root, 1200, 600);
    setScene(scene);

    setOnCloseRequest(_ -> timeline.stop());
    services.actionServices().positionWindow(this, "recentChanges");
  }

  private void refreshList(final boolean showProgress) {
    if (showProgress) {
      loading.set(true);
    }
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
    final Thread thread =
        new Thread(
            () -> {
              try (ProgressStep _ = progressTracker.start(GT._T("Loading recent changes"))) {
                runRefreshList(wiki, query, currentOptions, showProgress);
              }
            });
    thread.setDaemon(true);
    thread.start();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runRefreshList(
      final WikiDefinition wiki,
      final RecentChangesQuery query,
      final RecentChangesOptions options,
      final boolean showProgress) {
    try {
      final List<FilteredRecentChange> recentChanges =
          services.apiRecentChanges().retrieveRecentChanges(wiki, query).stream()
              .map(rc -> FilteredRecentChange.of(rc, wiki, options))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .sorted(FilteredRecentChangeComparator.INSTANCE)
              .toList();
      Platform.runLater(() -> updateTable(recentChanges, showProgress));
    } catch (final Exception e) {
      Platform.runLater(
          () -> {
            if (showProgress) {
              loading.set(false);
            }
          });
    }
  }

  private void updateTable(
      final List<FilteredRecentChange> recentChanges, final boolean showProgress) {
    int currentRowIndex = 0;
    for (final FilteredRecentChange rc : recentChanges) {
      while (currentRowIndex < tableItems.size()
          && FilteredRecentChangeComparator.INSTANCE.compare(rc, tableItems.get(currentRowIndex))
              > 0) {
        currentRowIndex++;
      }
      if (currentRowIndex >= tableItems.size()
          || FilteredRecentChangeComparator.INSTANCE.compare(rc, tableItems.get(currentRowIndex))
              != 0) {
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
    if (showProgress) {
      loading.set(false);
    }
  }
}
