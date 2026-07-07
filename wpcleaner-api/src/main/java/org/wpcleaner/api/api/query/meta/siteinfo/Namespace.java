package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record Namespace(
    @JsonProperty("canonical") @Nullable String canonical,
    @JsonProperty("case") @Nullable String caseType,
    @JsonProperty("content") @Nullable Boolean content,
    @JsonProperty("id") int id,
    @JsonProperty("*") @Nullable String local,
    @JsonProperty("name") @Nullable String name,
    @JsonProperty("subpages") @Nullable Boolean subpages) {}
