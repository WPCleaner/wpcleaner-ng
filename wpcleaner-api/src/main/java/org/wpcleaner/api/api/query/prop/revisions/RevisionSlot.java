package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record RevisionSlot(
    @JsonProperty("content") @Nullable String content,
    @JsonProperty("contentmodel") @Nullable String contentModel,
    @JsonProperty("sha1") @Nullable String sha1,
    @JsonProperty("size") @Nullable Integer size) {}
