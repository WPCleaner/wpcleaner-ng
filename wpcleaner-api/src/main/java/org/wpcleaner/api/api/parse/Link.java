package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Link(
    @JsonProperty("ns") int namespace,
    @JsonProperty("title") String title,
    @JsonProperty("exists") @Nullable Boolean exists) {}
