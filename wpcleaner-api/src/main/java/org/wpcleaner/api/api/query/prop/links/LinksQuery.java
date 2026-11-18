package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.prop.links.LinksParameters.Direction;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record LinksQuery(
    @Nullable Direction direction,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace,
    @Nullable String plContinue,
    @Nullable List<String> titles) {

  public Builder builder() {
    return emptyBuilder()
        .direction(direction)
        .limit(limit)
        .namespace(namespace)
        .plContinue(plContinue)
        .titles(titles);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Direction direction;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;
    @Nullable private String plContinue;
    @Nullable private List<String> titles;

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
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

    public Builder plContinue(@Nullable final String plContinue) {
      this.plContinue = plContinue;
      return this;
    }

    public Builder titles(@Nullable final List<String> titles) {
      this.titles = titles;
      return this;
    }

    public LinksQuery build() {
      return new LinksQuery(direction, limit, namespace, plContinue, titles);
    }
  }
}
