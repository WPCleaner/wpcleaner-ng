package org.wpcleaner.api.api.expandtemplates;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record ExpandTemplatesQuery(
    @Nullable Boolean includeComments,
    Set<ExpandTemplatesParameters.Properties> properties,
    @Nullable Integer revisionId,
    @Nullable Boolean showStrategyKeys,
    String text,
    @Nullable String title) {

  public ExpandTemplatesQuery {
    properties = Set.copyOf(properties);
  }

  public Builder builder() {
    return emptyBuilder()
        .includeComments(includeComments)
        .properties(properties)
        .revisionId(revisionId)
        .showStrategyKeys(showStrategyKeys)
        .text(text)
        .title(title);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Boolean includeComments;
    private Set<ExpandTemplatesParameters.Properties> properties = Set.of();
    @Nullable private Integer revisionId;
    @Nullable private Boolean showStrategyKeys;
    private String text = "";
    @Nullable private String title;

    public Builder includeComments(@Nullable final Boolean includeComments) {
      this.includeComments = includeComments;
      return this;
    }

    public Builder properties(final Set<ExpandTemplatesParameters.Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder revisionId(@Nullable final Integer revisionId) {
      this.revisionId = revisionId;
      return this;
    }

    public Builder showStrategyKeys(@Nullable final Boolean showStrategyKeys) {
      this.showStrategyKeys = showStrategyKeys;
      return this;
    }

    public Builder text(final String text) {
      this.text = text;
      return this;
    }

    public Builder title(@Nullable final String title) {
      this.title = title;
      return this;
    }

    public ExpandTemplatesQuery build() {
      return new ExpandTemplatesQuery(
          includeComments, properties, revisionId, showStrategyKeys, text, title);
    }
  }
}
