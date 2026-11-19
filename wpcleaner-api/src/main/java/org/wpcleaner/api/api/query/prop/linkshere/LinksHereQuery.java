package org.wpcleaner.api.api.query.prop.linkshere;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.prop.linkshere.LinksHereParameters.Properties;
import org.wpcleaner.api.api.query.prop.linkshere.LinksHereParameters.Show;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record LinksHereQuery(
    @Nullable String lhContinue,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace,
    @Nullable Set<Properties> properties,
    @Nullable Set<Show> show) {

  public Builder builder() {
    return emptyBuilder()
        .lhContinue(lhContinue)
        .limit(limit)
        .namespace(namespace)
        .properties(properties)
        .show(show);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private String lhContinue;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;
    @Nullable private Set<Properties> properties;
    @Nullable private Set<Show> show;

    public Builder lhContinue(@Nullable final String lhContinue) {
      this.lhContinue = lhContinue;
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

    public Builder show(@Nullable final Set<Show> show) {
      this.show = show;
      return this;
    }

    public LinksHereQuery build() {
      return new LinksHereQuery(lhContinue, limit, namespace, properties, show);
    }
  }
}
