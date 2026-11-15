package org.wpcleaner.api.api.query.meta.allmessages;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum AllMessagesParameters {
  ARGS("amargs"),
  CUSTOMISED("amcustomised"),
  ENABLE_PARSER("amenableparser"),
  FILTER("amfilter"),
  FROM("amfrom"),
  INCLUDE_LOCAL("amincludelocal"),
  LANGUAGE("amlang"),
  MESSAGES("ammessages"),
  NO_CONTENT("amnocontent"),
  PREFIX("amprefix"),
  PROPERTIES("amprop"),
  TITLE("amtitle"),
  TO("amto"),
  ;

  public final String value;

  AllMessagesParameters(final String value) {
    this.value = value;
  }

  public enum Customised {
    ALL("all"),
    MODIFIED("modified"),
    UNMODIFIED("unmodified"),
    ;

    public final String value;

    Customised(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    DEFAULT("default"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
