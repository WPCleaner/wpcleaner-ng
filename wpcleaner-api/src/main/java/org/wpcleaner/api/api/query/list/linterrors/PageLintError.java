package org.wpcleaner.api.api.query.list.linterrors;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.rest.lint.LintError;

public record PageLintError(
    @JsonProperty("category") String category,
    @JsonProperty("lintId") long lintId,
    @JsonProperty("location") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Integer> location,
    @JsonProperty("ns") int namespace,
    @JsonProperty("pageid") long pageId,
    @JsonProperty("params") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, @Nullable Object> parameters,
    @JsonProperty("templateInfo") LintError.@Nullable TemplateInfo templateInfo,
    @JsonProperty("title") String title) {}
