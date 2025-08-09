package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum ApiParameters {
  ACTION("action"),
  ASSERT("assert"),
  ASSERT_USER("assertuser"),
  CENTRAL_AUTH_TOKEN("centralauthtoken"),
  CROSS_ORIGIN("crossorigin"),
  CURRENT_TIMESTAMP("curtimestamp"),
  ERROR_FORMAT("errorformat"),
  ERROR_LANG("errorlang"),
  ERRORS_USE_LOCAL("errorsuselocal"),
  FORMAT("format"),
  MAX_AGE("maxage"),
  MAX_LAG("maxlag"),
  ORIGIN("origin"),
  REQUEST_ID("requestid"),
  RESPONSE_LANG_INFO("responselanginfo"),
  S_MAX_AGE("smaxage"),
  SERVED_BY("servedby"),
  USE_LANG("uselang"),
  VARIANT("variant");

  public final String value;

  ApiParameters(final String value) {
    this.value = value;
  }

  public enum Action {
    QUERY("query"),
    ;

    public final String value;

    Action(final String value) {
      this.value = value;
    }
  }

  public enum Format {
    JSON("json"),
    NONE("none"),
    PHP("php"),
    RAW_FM("rawfm"),
    XML("xml");

    public final String value;

    Format(final String value) {
      this.value = value;
    }
  }
}
