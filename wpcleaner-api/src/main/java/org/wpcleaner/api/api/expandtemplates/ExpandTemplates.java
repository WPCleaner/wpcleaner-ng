package org.wpcleaner.api.api.expandtemplates;

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
import org.wpcleaner.api.api.parse.Category;

public record ExpandTemplates(
    @JsonProperty("categories") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Category> categories,
    @JsonProperty("encodedjsconfigvars") @Nullable String encodedJsConfigVars,
    @JsonProperty("jsconfigvars") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, Object> jsConfigVars,
    @JsonProperty("modules") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> modules,
    @JsonProperty("modulescripts") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> moduleScripts,
    @JsonProperty("modulestyles") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> moduleStyles,
    @JsonProperty("parsetree") @Nullable String parseTree,
    @JsonProperty("properties") @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, String> properties,
    @JsonProperty("ttl") @Nullable Integer ttl,
    @JsonProperty("volatile") @Nullable Boolean volatileOutput,
    @JsonProperty("wikitext") @Nullable String wikiText) {}
