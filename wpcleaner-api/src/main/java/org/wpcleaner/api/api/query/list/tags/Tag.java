package org.wpcleaner.api.api.query.list.tags;

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

public record Tag(
    boolean active,
    boolean defined,
    @Nullable String description,
    @Nullable String displayName,
    @Nullable Integer hitCount,
    String name,
    List<String> source) {
  @JsonCreator
  public Tag(
      @JsonProperty("active") @Nullable final String active,
      @JsonProperty("defined") @Nullable final String defined,
      @JsonProperty("description") @Nullable final String description,
      @JsonProperty("displayname") @Nullable final String displayName,
      @JsonProperty("hitcount") @Nullable final Integer hitCount,
      @JsonProperty("name") final String name,
      @JsonProperty("source") @JsonSetter(nulls = Nulls.AS_EMPTY) final List<String> source) {
    this(
        Objects.nonNull(active),
        Objects.nonNull(defined),
        description,
        displayName,
        hitCount,
        name,
        Objects.requireNonNullElseGet(source, List::of));
  }
}
