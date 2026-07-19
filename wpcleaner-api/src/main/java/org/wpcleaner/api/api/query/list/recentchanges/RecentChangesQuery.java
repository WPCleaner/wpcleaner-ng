package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters.Direction;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters.Properties;

@SuppressWarnings({"PMD.TooManyFields", "PMD.AvoidFieldNameMatchingMethodName"})
public record RecentChangesQuery(
    @Nullable Direction direction,
    @Nullable Instant end,
    @Nullable String excludeUser,
    boolean generateRevisions,
    @Nullable String limit,
    @Nullable Set<Integer> namespace,
    @Nullable Set<Properties> properties,
    @Nullable String rccontinue,
    @Nullable Set<RecentChangesParameters.Show> show,
    @Nullable String slot,
    @Nullable Instant start,
    @Nullable String tag,
    @Nullable String title,
    boolean topOnly,
    @Nullable Set<RecentChangesParameters.Type> type,
    @Nullable String user) {

  public RecentChangesQuery {
    checkLimit(limit);
  }

  private static void checkLimit(@Nullable final String limit) {
    if (limit == null || Objects.equals(limit, "max")) {
      return;
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      Integer.parseUnsignedInt(limit);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "RecentChangesQuery.limit should be an integer or the String max", e);
    }
  }

  public Builder builder() {
    return emptyBuilder()
        .direction(direction)
        .end(end)
        .excludeUser(excludeUser)
        .generateRevisions(generateRevisions)
        .limit(limit)
        .namespace(namespace)
        .properties(properties)
        .rccontinue(rccontinue)
        .show(show)
        .slot(slot)
        .start(start)
        .tag(tag)
        .title(title)
        .topOnly(topOnly)
        .type(type)
        .user(user);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Direction direction;
    @Nullable private Instant end;
    @Nullable private String excludeUser;
    private boolean generateRevisions;
    @Nullable private String limit;
    @Nullable private Set<Integer> namespace;
    @Nullable private Set<Properties> properties;
    @Nullable private String rccontinue;
    @Nullable private Set<RecentChangesParameters.Show> show;
    @Nullable private String slot;
    @Nullable private Instant start;
    @Nullable private String tag;
    @Nullable private String title;
    private boolean topOnly;
    @Nullable private Set<RecentChangesParameters.Type> type;
    @Nullable private String user;

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder end(@Nullable final Instant end) {
      this.end = end;
      return this;
    }

    public Builder excludeUser(@Nullable final String excludeUser) {
      this.excludeUser = excludeUser;
      return this;
    }

    public Builder generateRevisions(final boolean generateRevisions) {
      this.generateRevisions = generateRevisions;
      return this;
    }

    public Builder limit(@Nullable final Integer limit) {
      this.limit = Optional.ofNullable(limit).map(Integer::toUnsignedString).orElse(null);
      return this;
    }

    public Builder limit(@Nullable final String limit) {
      this.limit = limit;
      return this;
    }

    public Builder namespace(@Nullable final Set<Integer> namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder properties(@Nullable final Set<Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder rccontinue(@Nullable final String rccontinue) {
      this.rccontinue = rccontinue;
      return this;
    }

    public Builder show(@Nullable final Set<RecentChangesParameters.Show> show) {
      this.show = show;
      return this;
    }

    public Builder slot(@Nullable final String slot) {
      this.slot = slot;
      return this;
    }

    public Builder start(@Nullable final Instant start) {
      this.start = start;
      return this;
    }

    public Builder tag(@Nullable final String tag) {
      this.tag = tag;
      return this;
    }

    public Builder title(@Nullable final String title) {
      this.title = title;
      return this;
    }

    public Builder topOnly(final boolean topOnly) {
      this.topOnly = topOnly;
      return this;
    }

    public Builder type(@Nullable final Set<RecentChangesParameters.Type> type) {
      this.type = type;
      return this;
    }

    public Builder user(@Nullable final String user) {
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
