package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record Extension(
    @JsonProperty("author") @Nullable String author,
    @JsonProperty("descriptionmsg") @Nullable String descriptionMsg,
    @JsonProperty("license") @Nullable String license,
    @JsonProperty("license-name") @Nullable String licenseName,
    @JsonProperty("name") String name,
    @JsonProperty("namemsg") @Nullable String nameMsg,
    @JsonProperty("type") @Nullable String type,
    @JsonProperty("url") @Nullable String url,
    @JsonProperty("vcs-date") @Nullable String vcsDate,
    @JsonProperty("vcs-system") @Nullable String vcsSystem,
    @JsonProperty("vcs-url") @Nullable String vcsUrl,
    @JsonProperty("vcs-version") @Nullable String vcsVersion,
    @JsonProperty("version") @Nullable String version) {}
