package org.wpcleaner.api.api.query.prop.linkshere;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum LinksHereParameters {
  CONTINUE("lhcontinue"),
  LIMIT("lhlimit"),
  NAMESPACE("lhnamespace"),
  PROPERTIES("lhprop"),
  SHOW("lhshow"),
  ;

  public final String value;

  LinksHereParameters(final String value) {
    this.value = value;
  }

  public enum Properties {
    PAGE_ID("pageid"),
    REDIRECT("redirect"),
    TITLE("title"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }

  public enum Show {
    NOT_REDIRECT("!redirect"),
    REDIRECT("redirect"),
    ;

    public final String value;

    Show(final String value) {
      this.value = value;
    }
  }
}
