package org.wpcleaner.api.api.query.prop.langlinks;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Langlink(
    @JsonProperty("autonym") @Nullable String autonym,
    @JsonProperty("lang") @Nullable String lang,
    @JsonProperty("langname") @Nullable String langName,
    @JsonProperty("title") @Nullable String title,
    @JsonProperty("url") @Nullable String url) {}
