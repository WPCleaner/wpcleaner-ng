package org.wpcleaner.api.api.login;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public record Login(
    @JsonProperty("result") String result,
    @JsonProperty("lguserid") Long userId,
    @JsonProperty("lgusername") String username) {

  public static final String RESULT_ABORTED = "Aborted";
  public static final String RESULT_FAILED = "Failed";
  public static final String RESULT_SUCCESS = "Success";
}
