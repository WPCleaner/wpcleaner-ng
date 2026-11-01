package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record Parse(
    @JsonProperty("title") String title,
    @JsonProperty("categories") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Category> categories,
    @JsonProperty("externallinks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> externalLinks,
    @JsonProperty("images") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> images,
    @JsonProperty("iwlinks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<InterwikiLink> interwikiLinks,
    @JsonProperty("langlinks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<LanguageLink> languageLinks,
    @JsonProperty("links") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Link> links,
    @JsonProperty("parsetree") @Nullable String parseTree,
    @JsonProperty("templates") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Link> templates,
    @JsonProperty("tocdata") @Nullable TableOfContents tableOfContents) {}
