package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record Tokens(
    @JsonProperty("createaccounttoken") @Nullable String createAccount,
    @JsonProperty("csrftoken") @Nullable String csrf,
    @JsonProperty("deleteglobalaccounttoken") @Nullable String deleteGlobalAccount,
    @JsonProperty("logintoken") @Nullable String login,
    @JsonProperty("patroltoken") @Nullable String patrol,
    @JsonProperty("rollbacktoken") @Nullable String rollback,
    @JsonProperty("setglobalaccountstatustoken") @Nullable String setGlobalAccountStatus,
    @JsonProperty("userrightstoken") @Nullable String userRights,
    @JsonProperty("watch") @Nullable String watch) {

  public static Tokens ofEmpty() {
    return new Tokens(null, null, null, null, null, null, null, null, null);
  }
}
