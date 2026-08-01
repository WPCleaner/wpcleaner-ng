package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record Page(
    @JsonProperty("missing") @Nullable String missing,
    @JsonProperty("ns") @Nullable Integer ns,
    @JsonProperty("pageid") @Nullable Integer pageId,
    @JsonProperty("revisions") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Revision> revisions,
    @JsonProperty("title") @Nullable String title) {

  public boolean isMissing() {
    return missing != null;
  }
}
