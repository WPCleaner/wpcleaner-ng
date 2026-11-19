package org.wpcleaner.api.api.query.list.embeddedin;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.embeddedin.EmbeddedInParameters.Direction;
import org.wpcleaner.api.api.query.list.embeddedin.EmbeddedInParameters.FilterRedirect;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record EmbeddedInQuery(
    @Nullable Direction direction,
    @Nullable String eiContinue,
    @Nullable FilterRedirect filterRedirect,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace) {

  public Builder builder() {
    return emptyBuilder()
        .direction(direction)
        .eiContinue(eiContinue)
        .filterRedirect(filterRedirect)
        .limit(limit)
        .namespace(namespace);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Direction direction;
    @Nullable private String eiContinue;
    @Nullable private FilterRedirect filterRedirect;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder eiContinue(@Nullable final String eiContinue) {
      this.eiContinue = eiContinue;
      return this;
    }

    public Builder filterRedirect(@Nullable final FilterRedirect filterRedirect) {
      this.filterRedirect = filterRedirect;
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

    public EmbeddedInQuery build() {
      return new EmbeddedInQuery(direction, eiContinue, filterRedirect, limit, namespace);
    }
  }
}
