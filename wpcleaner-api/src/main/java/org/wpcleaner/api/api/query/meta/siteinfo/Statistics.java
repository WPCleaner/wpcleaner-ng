package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public record Statistics(
    @JsonProperty("activeusers") int activeUsers,
    @JsonProperty("admins") int admins,
    @JsonProperty("articles") int articles,
    @JsonProperty("edits") int edits,
    @JsonProperty("images") int images,
    @JsonProperty("jobs") int jobs,
    @JsonProperty("pages") int pages,
    @JsonProperty("users") int users) {}
