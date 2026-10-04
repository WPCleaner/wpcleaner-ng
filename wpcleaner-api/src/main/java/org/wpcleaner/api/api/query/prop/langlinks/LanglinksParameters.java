package org.wpcleaner.api.api.query.prop.langlinks;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum LanglinksParameters {
  CONTINUE("llcontinue"),
  DIRECTION("lldir"),
  IN_LANGUAGE_CODE("llinlanguagecode"),
  LANGUAGE("lllang"),
  LIMIT("lllimit"),
  PROPERTIES("llprop"),
  TITLE("lltitle"),
  ;

  public final String value;

  LanglinksParameters(final String value) {
    this.value = value;
  }

  public enum Direction {
    ASCENDING("ascending"),
    DESCENDING("descending"),
    ;

    public final String value;

    Direction(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    AUTONYM("autonym"),
    LANGUAGE_NAME("langname"),
    URL("url"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
