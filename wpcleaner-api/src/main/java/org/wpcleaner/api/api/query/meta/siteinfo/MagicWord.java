package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.List;

public record MagicWord(
    @JsonProperty("aliases") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> aliases,
    @JsonProperty("case-sensitive") @Nullable Boolean caseSensitive,
    @JsonProperty("name") String name) {}
