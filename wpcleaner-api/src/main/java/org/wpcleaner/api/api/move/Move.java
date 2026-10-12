package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Move(
    @JsonProperty("from") String from,
    @JsonProperty("reason") @Nullable String reason,
    @JsonProperty("redirectcreated") @Nullable Boolean redirectCreated,
    @JsonProperty("to") String to) {}
