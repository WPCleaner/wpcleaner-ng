package org.wpcleaner.api.api.query.list.search;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.search.SearchParameters.Info;
import org.wpcleaner.api.api.query.list.search.SearchParameters.Properties;
import org.wpcleaner.api.api.query.list.search.SearchParameters.QueryDependentProfile;
import org.wpcleaner.api.api.query.list.search.SearchParameters.QueryIndependentProfile;
import org.wpcleaner.api.api.query.list.search.SearchParameters.Sort;
import org.wpcleaner.api.api.query.list.search.SearchParameters.What;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record SearchQuery(
    boolean enableRewrites,
    @Nullable Set<Info> info,
    boolean interwiki,
    @Nullable Limit limit,
    @Nullable Set<Namespace> namespace,
    @Nullable Integer offset,
    @Nullable Set<Properties> properties,
    @Nullable QueryDependentProfile queryDependentProfile,
    @Nullable QueryIndependentProfile queryIndependentProfile,
    @Nullable Sort sort,
    @Nullable What what) {

  public Builder builder() {
    return emptyBuilder()
        .enableRewrites(enableRewrites)
        .info(info)
        .interwiki(interwiki)
        .limit(limit)
        .namespace(namespace)
        .offset(offset)
        .properties(properties)
        .queryDependentProfile(queryDependentProfile)
        .queryIndependentProfile(queryIndependentProfile)
        .sort(sort)
        .what(what);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    private boolean enableRewrites;
    @Nullable private Set<Info> info;
    private boolean interwiki;
    @Nullable private Limit limit;
    @Nullable private Set<Namespace> namespace;
    @Nullable private Integer offset;
    @Nullable private Set<Properties> properties;
    @Nullable private QueryDependentProfile queryDependentProfile;
    @Nullable private QueryIndependentProfile queryIndependentProfile;
    @Nullable private Sort sort;
    @Nullable private What what;

    public Builder enableRewrites(final boolean enableRewrites) {
      this.enableRewrites = enableRewrites;
      return this;
    }

    public Builder info(@Nullable final Set<Info> info) {
      this.info = info;
      return this;
    }

    public Builder interwiki(final boolean interwiki) {
      this.interwiki = interwiki;
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

    public Builder offset(@Nullable final Integer offset) {
      this.offset = offset;
      return this;
    }

    public Builder properties(@Nullable final Set<Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder queryDependentProfile(
        @Nullable final QueryDependentProfile queryDependentProfile) {
      this.queryDependentProfile = queryDependentProfile;
      return this;
    }

    public Builder queryIndependentProfile(
        @Nullable final QueryIndependentProfile queryIndependentProfile) {
      this.queryIndependentProfile = queryIndependentProfile;
      return this;
    }

    public Builder sort(@Nullable final Sort sort) {
      this.sort = sort;
      return this;
    }

    public Builder what(@Nullable final What what) {
      this.what = what;
      return this;
    }

    public SearchQuery build() {
      return new SearchQuery(
          enableRewrites,
          info,
          interwiki,
          limit,
          namespace,
          offset,
          properties,
          queryDependentProfile,
          queryIndependentProfile,
          sort,
          what);
    }
  }
}
