package org.wpcleaner.api.api.query.prop.langlinks;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record Page(
    List<Langlink> langlinks,
    boolean missing,
    @Nullable Integer ns,
    @Nullable Integer pageId,
    @Nullable String title) {
  @JsonCreator
  public Page(
      @JsonProperty("langlinks") @JsonSetter(nulls = Nulls.AS_EMPTY) final List<Langlink> langlinks,
      @JsonProperty("missing") @Nullable final String missing,
      @JsonProperty("ns") @Nullable final Integer ns,
      @JsonProperty("pageid") @Nullable final Integer pageId,
      @JsonProperty("title") @Nullable final String title) {
    this(
        Objects.requireNonNullElseGet(langlinks, List::of),
        Objects.nonNull(missing),
        ns,
        pageId,
        title);
  }
}
