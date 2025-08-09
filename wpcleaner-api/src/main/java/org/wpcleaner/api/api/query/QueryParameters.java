package org.wpcleaner.api.api.query;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum QueryParameters {
  CONTINUE("continue"),
  CONVERT_TITLES("converttitles"),
  EXPORT("export"),
  EXPORT_NO_WRAP("exportnowrap"),
  EXPORT_SCHEMA("exportschema"),
  GENERATOR("generator"),
  INDEX_PAGE_IDS("indexpageids"),
  INTERWIKI_URL("iwurl"),
  LIST("list"),
  META("meta"),
  PAGE_IDS("pageids"),
  PROPERTIES("prop"),
  RAW_CONTINUE("rawcontinue"),
  REDIRECTS("redirects"),
  REVISION_IDS("revids"),
  TITLES("titles");

  public final String value;

  QueryParameters(final String value) {
    this.value = value;
  }

  public enum Meta {
    TOKENS("tokens");

    public final String value;

    Meta(final String value) {
      this.value = value;
    }
  }
}
