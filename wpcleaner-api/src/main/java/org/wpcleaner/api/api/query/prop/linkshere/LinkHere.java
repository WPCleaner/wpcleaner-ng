package org.wpcleaner.api.api.query.prop.linkshere;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record LinkHere(
    @JsonProperty("ns") int ns,
    @JsonProperty("pageid") @Nullable Integer pageId,
    @JsonProperty("redirect") @Nullable Boolean redirect,
    @JsonProperty("title") @Nullable String title) {}
