package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;

@SuppressWarnings({"PMD.TooManyFields", "PMD.AvoidFieldNameMatchingMethodName"})
public record RecentChangesQuery(
    @Nullable RecentChangesParameters.Direction direction,
    @Nullable Instant end,
    @Nullable String excludeUser,
    @Nullable Boolean generateRevisions,
    @Nullable Integer limit,
    @Nullable List<Integer> namespace,
    @Nullable List<RecentChangesParameters.Properties> properties,
    @Nullable String rccontinue,
    @Nullable List<RecentChangesParameters.Show> show,
    @Nullable String slot,
    @Nullable Instant start,
    @Nullable String tag,
    @Nullable String title,
    @Nullable Boolean topOnly,
    @Nullable List<RecentChangesParameters.Type> type,
    @Nullable String user) {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private RecentChangesParameters.Direction direction;
    @Nullable private Instant end;
    @Nullable private String excludeUser;
    @Nullable private Boolean generateRevisions;
    @Nullable private Integer limit;
    @Nullable private List<Integer> namespace;
    @Nullable private List<RecentChangesParameters.Properties> properties;
    @Nullable private String rccontinue;
    @Nullable private List<RecentChangesParameters.Show> show;
    @Nullable private String slot;
    @Nullable private Instant start;
    @Nullable private String tag;
    @Nullable private String title;
    @Nullable private Boolean topOnly;
    @Nullable private List<RecentChangesParameters.Type> type;
    @Nullable private String user;

    public Builder direction(final RecentChangesParameters.Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder end(final Instant end) {
      this.end = end;
      return this;
    }

    public Builder excludeUser(final String excludeUser) {
      this.excludeUser = excludeUser;
      return this;
    }

    public Builder generateRevisions(final Boolean generateRevisions) {
      this.generateRevisions = generateRevisions;
      return this;
    }

    public Builder limit(final Integer limit) {
      this.limit = limit;
      return this;
    }

    public Builder namespace(final List<Integer> namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder properties(final List<RecentChangesParameters.Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder rccontinue(final String rccontinue) {
      this.rccontinue = rccontinue;
      return this;
    }

    public Builder show(final List<RecentChangesParameters.Show> show) {
      this.show = show;
      return this;
    }

    public Builder slot(final String slot) {
      this.slot = slot;
      return this;
    }

    public Builder start(final Instant start) {
      this.start = start;
      return this;
    }

    public Builder tag(final String tag) {
      this.tag = tag;
      return this;
    }

    public Builder title(final String title) {
      this.title = title;
      return this;
    }

    public Builder topOnly(final Boolean topOnly) {
      this.topOnly = topOnly;
      return this;
    }

    public Builder type(final List<RecentChangesParameters.Type> type) {
      this.type = type;
      return this;
    }

    public Builder user(final String user) {
      this.user = user;
      return this;
    }

    public RecentChangesQuery build() {
      return new RecentChangesQuery(
          direction,
          end,
          excludeUser,
          generateRevisions,
          limit,
          namespace,
          properties,
          rccontinue,
          show,
          slot,
          start,
          tag,
          title,
          topOnly,
          type,
          user);
    }
  }
}
