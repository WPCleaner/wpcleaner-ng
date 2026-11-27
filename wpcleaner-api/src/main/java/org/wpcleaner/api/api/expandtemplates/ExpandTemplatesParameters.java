package org.wpcleaner.api.api.expandtemplates;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum ExpandTemplatesParameters {
  INCLUDE_COMMENTS("includecomments"),
  PROPERTIES("prop"),
  REVISION_ID("revid"),
  SHOW_STRATEGY_KEYS("showstrategykeys"),
  TEXT("text"),
  TITLE("title"),
  ;

  public final String value;

  ExpandTemplatesParameters(final String value) {
    this.value = value;
  }

  public enum Properties {
    CATEGORIES("categories"),
    ENCODED_JS_CONFIG_VARS("encodedjsconfigvars"),
    JS_CONFIG_VARS("jsconfigvars"),
    MODULES("modules"),
    PARSE_TREE("parsetree"),
    PROPERTIES("properties"),
    TTL("ttl"),
    VOLATILE("volatile"),
    WIKITEXT("wikitext"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
