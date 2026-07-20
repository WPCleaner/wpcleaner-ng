package org.wpcleaner.api.api.query.list.tags;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum TagsParameters {
  CONTINUE("tgcontinue"),
  LIMIT("tglimit"),
  PROPERTIES("tgprop"),
  ;

  public final String value;

  TagsParameters(final String value) {
    this.value = value;
  }

  public enum Properties {
    ACTIVE("active"),
    DEFINED("defined"),
    DESCRIPTION("description"),
    DISPLAY_NAME("displayName"),
    HIT_COUNT("hitcount"),
    SOURCE("source"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }
}
