package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record CategoryMember(
    @JsonProperty("ns") int ns,
    @JsonProperty("pageid") @Nullable Integer pageId,
    @JsonProperty("sortkey") @Nullable String sortKey,
    @JsonProperty("sortkeyprefix") @Nullable String sortKeyPrefix,
    @JsonProperty("timestamp") @Nullable Instant timestamp,
    @JsonProperty("title") @Nullable String title,
    @JsonProperty("type") @Nullable String type) {}
