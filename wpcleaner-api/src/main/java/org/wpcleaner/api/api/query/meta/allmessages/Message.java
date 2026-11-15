package org.wpcleaner.api.api.query.meta.allmessages;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Message(
    @JsonProperty("content") @Nullable String content,
    @JsonProperty("customised") @Nullable Boolean customised,
    @JsonProperty("defaultmissing") @Nullable Boolean defaultMissing,
    @JsonProperty("default") @Nullable String defaultValue,
    @JsonProperty("missing") @Nullable Boolean missing,
    @JsonProperty("name") String name,
    @JsonProperty("normalizedname") String normalizedName) {}
