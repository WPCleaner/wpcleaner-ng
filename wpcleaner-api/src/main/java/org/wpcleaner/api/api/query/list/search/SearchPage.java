package org.wpcleaner.api.api.query.list.search;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public record SearchPage(
    @JsonProperty("categorysnippet") @Nullable String categorySnippet,
    @JsonProperty("extensiondata") @Nullable Map<String, Object> extensionData,
    @JsonProperty("isfilematch") @Nullable Boolean isFileMatch,
    @JsonProperty("ns") int ns,
    @JsonProperty("pageid") int pageId,
    @JsonProperty("redirectsnippet") @Nullable String redirectSnippet,
    @JsonProperty("redirecttitle") @Nullable String redirectTitle,
    @JsonProperty("sectionsnippet") @Nullable String sectionSnippet,
    @JsonProperty("sectiontitle") @Nullable String sectionTitle,
    @JsonProperty("size") @Nullable Integer size,
    @JsonProperty("snippet") @Nullable String snippet,
    @JsonProperty("timestamp") @Nullable Instant timestamp,
    @JsonProperty("title") String title,
    @JsonProperty("titlesnippet") @Nullable String titleSnippet,
    @JsonProperty("wordcount") @Nullable Integer wordCount) {}
