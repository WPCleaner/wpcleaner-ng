package org.wpcleaner.api.api.query.list.tags;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record Tag(
    @JsonProperty("active") @Nullable String active,
    @JsonProperty("defined") @Nullable String defined,
    @JsonProperty("description") @Nullable String description,
    @JsonProperty("displayname") @Nullable String displayName,
    @JsonProperty("hitcount") @Nullable Integer hitCount,
    @JsonProperty("name") String name,
    @JsonProperty("source") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> source) {

  public boolean isActive() {
    return active != null;
  }

  public boolean isDefined() {
    return defined != null;
  }
}
