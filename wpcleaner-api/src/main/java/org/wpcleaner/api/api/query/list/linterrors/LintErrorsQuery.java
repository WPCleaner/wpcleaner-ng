package org.wpcleaner.api.api.query.list.linterrors;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record LintErrorsQuery(
    @Nullable Set<String> categories,
    @Nullable Long from,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace,
    @Nullable Set<Long> pageId,
    @Nullable String title) {

  public Builder builder() {
    return emptyBuilder()
        .categories(categories)
        .from(from)
        .limit(limit)
        .namespace(namespace)
        .pageId(pageId)
        .title(title);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Set<String> categories;
    @Nullable private Long from;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;
    @Nullable private Set<Long> pageId;
    @Nullable private String title;

    public Builder categories(@Nullable final Set<String> categories) {
      this.categories = categories;
      return this;
    }

    public Builder from(@Nullable final Long from) {
      this.from = from;
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

    public Builder pageId(@Nullable final Set<Long> pageId) {
      this.pageId = pageId;
      return this;
    }

    public Builder title(@Nullable final String title) {
      this.title = title;
      return this;
    }

    public LintErrorsQuery build() {
      return new LintErrorsQuery(categories, from, limit, namespace, pageId, title);
    }
  }
}
