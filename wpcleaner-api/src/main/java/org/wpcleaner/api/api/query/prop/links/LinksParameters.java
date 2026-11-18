package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum LinksParameters {
  CONTINUE("plcontinue"),
  DIRECTION("pldir"),
  LIMIT("pllimit"),
  NAMESPACE("plnamespace"),
  TITLES("pltitles"),
  ;

  public final String value;

  LinksParameters(final String value) {
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
}
