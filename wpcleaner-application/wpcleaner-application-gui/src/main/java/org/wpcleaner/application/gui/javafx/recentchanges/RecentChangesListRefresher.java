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
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class RecentChangesListRefresher {

  private static final int MAX_RECENT_CHANGES = 1000;
  private static final Duration RECENT_CHANGES_OVERLAP = Duration.ofSeconds(10);
  private static final RecentChangesQuery DEFAULT_QUERY =
      RecentChangesQuery.emptyBuilder()
          .limit(Limit.max())
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
  private final RecentChangesOptionsInput optionsInput;
  private final ObservableList<FilteredRecentChange> tableItems;
  private final JavaFxProgressTracker progressTracker;
  private final BooleanProperty loading;
  @Nullable private Instant lastRecentChange;

  public RecentChangesListRefresher(
      final JavaFxRecentChangesWindowServices services,
      final RecentChangesOptionsInput optionsInput,
      final ObservableList<FilteredRecentChange> tableItems,
      final JavaFxProgressTracker progressTracker,
      final BooleanProperty loading) {
    this.services = services;
    this.optionsInput = optionsInput;
    this.tableItems = tableItems;
    this.progressTracker = progressTracker;
    this.loading = loading;
  }

  public void refreshList(final boolean showProgress) {
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
