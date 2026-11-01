package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public record InterwikiLink(
    @JsonProperty("prefix") String prefix,
    @JsonProperty("title") String title,
    @JsonProperty("url") String url) {}
