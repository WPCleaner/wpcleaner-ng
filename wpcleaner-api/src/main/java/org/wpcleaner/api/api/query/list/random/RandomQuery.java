package org.wpcleaner.api.api.query.list.random;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.random.RandomParameters.ContentModel;
import org.wpcleaner.api.api.query.list.random.RandomParameters.FilterRedirect;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record RandomQuery(
    @Nullable ContentModel contentModel,
    @Nullable FilterRedirect filterRedirect,
    @Nullable Limit limit,
    @Nullable Integer maxSize,
    @Nullable Integer minSize,
    @Nullable Set<Namespace> namespace,
    @Nullable String rnContinue) {

  public Builder builder() {
    return emptyBuilder()
        .contentModel(contentModel)
        .filterRedirect(filterRedirect)
        .limit(limit)
        .maxSize(maxSize)
        .minSize(minSize)
        .namespace(namespace)
        .rnContinue(rnContinue);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private ContentModel contentModel;
    @Nullable private FilterRedirect filterRedirect;
    @Nullable private Limit limit;
    @Nullable private Integer maxSize;
    @Nullable private Integer minSize;
    @Nullable private Set<Namespace> namespace;
    @Nullable private String rnContinue;

    public Builder contentModel(@Nullable final ContentModel contentModel) {
      this.contentModel = contentModel;
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

    public Builder maxSize(@Nullable final Integer maxSize) {
      this.maxSize = maxSize;
      return this;
    }

    public Builder minSize(@Nullable final Integer minSize) {
      this.minSize = minSize;
      return this;
    }

    public Builder namespace(@Nullable final Set<Namespace> namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder rnContinue(@Nullable final String rnContinue) {
      this.rnContinue = rnContinue;
      return this;
    }

    public RandomQuery build() {
      return new RandomQuery(
          contentModel, filterRedirect, limit, maxSize, minSize, namespace, rnContinue);
    }
  }
}
