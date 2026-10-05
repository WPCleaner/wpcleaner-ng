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
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesOptions;

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

  private final JavaFxWindow<?> owner;
  private final JavaFxRecentChangesWindowServices services;
  private final RecentChangesOptionsInput optionsInput;
  private final ObservableList<FilteredRecentChange> tableItems;
  @Nullable private Instant lastRecentChange;

  public RecentChangesListRefresher(
      final JavaFxWindow<?> owner,
      final JavaFxRecentChangesWindowServices services,
      final RecentChangesOptionsInput optionsInput,
      final ObservableList<FilteredRecentChange> tableItems) {
    this.owner = owner;
    this.services = services;
    this.optionsInput = optionsInput;
    this.tableItems = tableItems;
  }

  public void refreshList(final boolean showProgress) {
    owner.executeAsync(new RefreshListTask(showProgress), this::updateTable, _ -> {});
  }

  private final class RefreshListTask implements LongRunningTask<List<FilteredRecentChange>> {

    private final boolean displayProgress;

    RefreshListTask(final boolean showProgress) {
      this.displayProgress = showProgress;
    }

    @Override
    public boolean showProgress() {
      return displayProgress;
    }

    @Override
    public List<FilteredRecentChange> call(final ProgressTracker tracker) {
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
      try (ProgressStep _ = tracker.start(GT._T("Loading recent changes"))) {
        final List<RecentChange> rawRecentChanges =
            services.apiRecentChanges().retrieveRecentChanges(wiki, query);
        return rawRecentChanges.stream()
            .map(rc -> FilteredRecentChange.of(rc, wiki, currentOptions, rawRecentChanges))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(FilteredRecentChangeComparator.INSTANCE)
            .toList();
      }
    }
  }

  private void updateTable(final List<FilteredRecentChange> recentChanges) {
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
  }
}
