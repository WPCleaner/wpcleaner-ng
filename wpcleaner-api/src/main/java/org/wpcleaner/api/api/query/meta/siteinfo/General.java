package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record General(
    @JsonProperty("articlepath") @Nullable String articlePath,
    @JsonProperty("base") @Nullable String base,
    @JsonProperty("case") @Nullable String caseType,
    @JsonProperty("dbtype") @Nullable String dbType,
    @JsonProperty("dbversion") @Nullable String dbVersion,
    @JsonProperty("generator") @Nullable String generator,
    @JsonProperty("lang") @Nullable String lang,
    @JsonProperty("logo") @Nullable String logo,
    @JsonProperty("mainpage") @Nullable String mainPage,
    @JsonProperty("maxarticlesize") @Nullable Long maxArticleSize,
    @JsonProperty("server") @Nullable String server,
    @JsonProperty("servername") @Nullable String serverName,
    @JsonProperty("sitename") @Nullable String siteName,
    @JsonProperty("timezone") @Nullable String timeZone,
    @JsonProperty("wikiid") @Nullable String wikiId) {}
