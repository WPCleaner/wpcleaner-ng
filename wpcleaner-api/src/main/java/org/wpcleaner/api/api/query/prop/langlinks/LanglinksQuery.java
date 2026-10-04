package org.wpcleaner.api.api.query.prop.langlinks;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksParameters.Direction;
import org.wpcleaner.api.api.query.prop.langlinks.LanglinksParameters.Properties;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record LanglinksQuery(
    @Nullable Direction direction,
    @Nullable String inLanguageCode,
    @Nullable String language,
    @Nullable Limit limit,
    @Nullable String llContinue,
    @Nullable Set<Properties> properties,
    @Nullable String title) {

  public Builder builder() {
    return emptyBuilder()
        .direction(direction)
        .inLanguageCode(inLanguageCode)
        .language(language)
        .limit(limit)
        .llContinue(llContinue)
        .properties(properties)
        .title(title);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Direction direction;
    @Nullable private String inLanguageCode;
    @Nullable private String language;
    @Nullable private Limit limit;
    @Nullable private String llContinue;
    @Nullable private Set<Properties> properties;
    @Nullable private String title;

    public Builder direction(@Nullable final Direction direction) {
      this.direction = direction;
      return this;
    }

    public Builder inLanguageCode(@Nullable final String inLanguageCode) {
      this.inLanguageCode = inLanguageCode;
      return this;
    }

    public Builder language(@Nullable final String language) {
      this.language = language;
      return this;
    }

    public Builder limit(@Nullable final Limit limit) {
      this.limit = limit;
      return this;
    }

    public Builder llContinue(@Nullable final String llContinue) {
      this.llContinue = llContinue;
      return this;
    }

    public Builder properties(@Nullable final Set<Properties> properties) {
      this.properties = properties;
      return this;
    }

    public Builder title(@Nullable final String title) {
      this.title = title;
      return this;
    }

    public LanglinksQuery build() {
      return new LanglinksQuery(
          direction, inLanguageCode, language, limit, llContinue, properties, title);
    }
  }
}
