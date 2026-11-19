package org.wpcleaner.api.api.query.list.embeddedin;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum EmbeddedInParameters {
  CONTINUE("eicontinue"),
  DIRECTION("eidir"),
  FILTER_REDIRECT("eifilterredir"),
  LIMIT("eilimit"),
  NAMESPACE("einamespace"),
  PAGE_ID("eipageid"),
  TITLE("eititle"),
  ;

  public final String value;

  EmbeddedInParameters(final String value) {
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

  public enum FilterRedirect {
    ALL("all"),
    NON_REDIRECTS("nonredirects"),
    REDIRECTS("redirects"),
    ;

    public final String value;

    FilterRedirect(final String value) {
      this.value = value;
    }
  }
}
