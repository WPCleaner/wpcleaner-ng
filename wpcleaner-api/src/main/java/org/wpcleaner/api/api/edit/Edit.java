package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Edit(
    @JsonProperty("result") String result,
    @JsonProperty("pageid") @Nullable Integer pageId,
    @JsonProperty("title") @Nullable String title,
    @JsonProperty("nochange") @Nullable Boolean noChange) {

  public static final String RESULT_SUCCESS = "Success";
}
