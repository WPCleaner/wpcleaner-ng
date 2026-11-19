package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMembersParameters.Direction;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMembersParameters.Properties;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMembersParameters.Sort;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record CategoryMembersQuery(
    @Nullable String cmContinue,
    @Nullable Direction direction,
    @Nullable Instant end,
    @Nullable String endHexSortKey,
    @Nullable String endSortKeyPrefix,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace,
    @Nullable Set<Properties> properties,
    @Nullable Sort sort,
    @Nullable Instant start,
    @Nullable String startHexSortKey,
    @Nullable String startSortKeyPrefix,
    @Nullable Set<CategoryMembersParameters.Type> type) {

  public Builder builder() {
    return emptyBuilder()
        .cmContinue(cmContinue)
        .direction(direction)
        .end(end)
        .endHexSortKey(endHexSortKey)
        .endSortKeyPrefix(endSortKeyPrefix)
        .limit(limit)
        .namespace(namespace)
        .properties(properties)
        .sort(sort)
        .start(start)
        .startHexSortKey(startHexSortKey)
        .startSortKeyPrefix(startSortKeyPrefix)
        .type(type);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private String cmContinue;
    @Nullable private Direction direction;
    @Nullable private Instant end;
    @Nullable private String endHexSortKey;
    @Nullable private String endSortKeyPrefix;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;
    @Nullable private Set<Properties> properties;
    @Nullable private Sort sort;
    @Nullable private Instant start;
    @Nullable private String startHexSortKey;
    @Nullable private String startSortKeyPrefix;
    @Nullable private Set<CategoryMembersParameters.Type> type;

    public Builder cmContinue(@Nullable final String cmContinue) {
      this.cmContinue = cmContinue;
      return this;
    }

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder end(@Nullable final Instant end) {
      this.end = end;
      return this;
    }

    public Builder endHexSortKey(@Nullable final String endHexSortKey) {
      this.endHexSortKey = endHexSortKey;
      return this;
    }

    public Builder endSortKeyPrefix(@Nullable final String endSortKeyPrefix) {
      this.endSortKeyPrefix = endSortKeyPrefix;
      return this;
    }

    public Builder limit(@Nullable final Limit limit) {
      this.limit = limit;
      return this;
    }

    public Builder namespace(@Nullable final Set<Namespace> namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder properties(@Nullable final Set<Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder sort(@Nullable final Sort sort) {
      this.sort = sort;
      return this;
    }

    public Builder start(@Nullable final Instant start) {
      this.start = start;
      return this;
    }

    public Builder startHexSortKey(@Nullable final String startHexSortKey) {
      this.startHexSortKey = startHexSortKey;
      return this;
    }

    public Builder startSortKeyPrefix(@Nullable final String startSortKeyPrefix) {
      this.startSortKeyPrefix = startSortKeyPrefix;
      return this;
    }

    public Builder type(@Nullable final Set<CategoryMembersParameters.Type> type) {
      this.type = type;
      return this;
    }

    public CategoryMembersQuery build() {
      return new CategoryMembersQuery(
          cmContinue,
          direction,
          end,
          endHexSortKey,
          endSortKeyPrefix,
          limit,
          namespace,
          properties,
          sort,
          start,
          startHexSortKey,
          startSortKeyPrefix,
          type);
    }
  }
}
