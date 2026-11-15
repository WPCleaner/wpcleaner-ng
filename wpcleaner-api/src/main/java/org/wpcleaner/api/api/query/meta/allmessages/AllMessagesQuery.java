package org.wpcleaner.api.api.query.meta.allmessages;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.meta.allmessages.AllMessagesParameters.Customised;
import org.wpcleaner.api.api.query.meta.allmessages.AllMessagesParameters.Properties;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public record AllMessagesQuery(
    @Nullable List<String> args,
    @Nullable Customised customised,
    boolean enableParser,
    @Nullable String filter,
    @Nullable String from,
    boolean includeLocal,
    @Nullable String language,
    @Nullable Set<String> messages,
    boolean noContent,
    @Nullable String prefix,
    @Nullable Set<Properties> properties,
    @Nullable String title,
    @Nullable String to) {

  public Builder builder() {
    return emptyBuilder()
        .args(args)
        .customised(customised)
        .enableParser(enableParser)
        .filter(filter)
        .from(from)
        .includeLocal(includeLocal)
        .language(language)
        .messages(messages)
        .noContent(noContent)
        .prefix(prefix)
        .properties(properties)
        .title(title)
        .to(to);
  }

  public static Builder emptyBuilder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private List<String> args;
    @Nullable private Customised customised;
    private boolean enableParser;
    @Nullable private String filter;
    @Nullable private String from;
    private boolean includeLocal;
    @Nullable private String language;
    @Nullable private Set<String> messages;
    private boolean noContent;
    @Nullable private String prefix;
    @Nullable private Set<Properties> properties;
    @Nullable private String title;
    @Nullable private String to;

    public Builder args(@Nullable final List<String> args) {
      this.args = args;
      return this;
    }

    public Builder customised(@Nullable final Customised customised) {
      this.customised = customised;
      return this;
    }

    public Builder enableParser(final boolean enableParser) {
      this.enableParser = enableParser;
      return this;
    }

    public Builder filter(@Nullable final String filter) {
      this.filter = filter;
      return this;
    }

    public Builder from(@Nullable final String from) {
      this.from = from;
      return this;
    }

    public Builder includeLocal(final boolean includeLocal) {
      this.includeLocal = includeLocal;
      return this;
    }

    public Builder language(@Nullable final String language) {
      this.language = language;
      return this;
    }

    public Builder messages(@Nullable final Set<String> messages) {
      this.messages = messages;
      return this;
    }

    public Builder noContent(final boolean noContent) {
      this.noContent = noContent;
      return this;
    }

    public Builder prefix(@Nullable final String prefix) {
      this.prefix = prefix;
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

    public Builder to(@Nullable final String to) {
      this.to = to;
      return this;
    }

    public AllMessagesQuery build() {
      return new AllMessagesQuery(
          args,
          customised,
          enableParser,
          filter,
          from,
          includeLocal,
          language,
          messages,
          noContent,
          prefix,
          properties,
          title,
          to);
    }
  }
}
