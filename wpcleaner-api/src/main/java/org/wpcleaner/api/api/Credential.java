package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record Credential(
    @JsonProperty(value = "username", required = true) String username,
    @JsonProperty(value = "password", required = true) String password,
    @JsonProperty("wiki") @Nullable WikiDefinition wiki) {}
