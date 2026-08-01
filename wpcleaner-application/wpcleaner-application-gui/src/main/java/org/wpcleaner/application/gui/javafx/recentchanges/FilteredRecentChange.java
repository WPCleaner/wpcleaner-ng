package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record FilteredRecentChange(
    String comment,
    @Nullable Integer delta,
    @Nullable URI diffURI,
    RecentChangesFilter filter,
    @Nullable URI pageURI,
    @Nullable Integer rcid,
    @Nullable Integer revid,
    @Nullable Integer oldRevid,
    List<String> tags,
    @Nullable Instant timestamp,
    String title,
    String user) {

  public static Optional<FilteredRecentChange> of(
      final RecentChange rc, final WikiDefinition wiki, final RecentChangesOptions options) {
    final Optional<RecentChangesFilter> filter = options.matchesFilters(rc);
    if (filter.isEmpty()) {
      return Optional.empty();
    }
    final Integer delta =
        (rc.newLen() != null && rc.oldLen() != null) ? (rc.newLen() - rc.oldLen()) : null;
    return Optional.of(
        new FilteredRecentChange(
            Objects.requireNonNullElse(rc.comment(), ""),
            delta,
            buildDiffURI(rc, wiki),
            filter.get(),
            buildPageURI(rc, wiki),
            rc.rcid(),
            rc.revid(),
            rc.oldRevid(),
            Objects.requireNonNullElseGet(rc.tags(), List::of),
            rc.timestamp(),
            Objects.requireNonNullElse(rc.title(), ""),
            Objects.requireNonNullElse(rc.user(), "")));
  }

  @Nullable
  private static URI buildPageURI(final RecentChange rc, final WikiDefinition wiki) {
    if (rc.title() == null) {
      return null;
    }
    try {
      final String path = wiki.wikiPath() + "/" + rc.title().replace(' ', '_');
      return new URI("https", wiki.mainHost(), path, null);
    } catch (final URISyntaxException e) {
      return null;
    }
  }

  @Nullable
  private static URI buildDiffURI(final RecentChange rc, final WikiDefinition wiki) {
    if (rc.revid() == null || !Objects.equals(rc.type(), RecentChangesParameters.Type.EDIT.value)) {
      return null;
    }
    try {
      final String path = wiki.wikiPath() + "/Special:Diff/" + rc.revid();
      return new URI("https", wiki.mainHost(), path, null);
    } catch (final URISyntaxException e) {
      return null;
    }
  }
}
