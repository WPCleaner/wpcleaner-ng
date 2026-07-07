package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record UserGroup(
    @JsonProperty("name") String name,
    @JsonProperty("number") @Nullable Integer number,
    @JsonProperty("rights") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> rights) {}
