package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum ParseParameters {
  CONTENT_MODEL("contentmodel"),
  PROPERTIES("prop"),
  TEXT("text"),
  TITLE("title"),
  ;

  public static final String WIKITEXT_CONTENT_MODEL = "wikitext";

  public final String value;

  ParseParameters(final String value) {
    this.value = value;
  }

  public enum Properties {
    CATEGORIES("categories"),
    EXTERNAL_LINKS("externallinks"),
    IMAGES("images"),
    INTERWIKI_LINKS("iwlinks"),
    LANGUAGE_LINKS("langlinks"),
    LINKS("links"),
    PARSE_TREE("parsetree"),
    TEMPLATES("templates"),
    TOC_DATA("tocdata"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
