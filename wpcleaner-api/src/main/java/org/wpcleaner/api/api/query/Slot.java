package org.wpcleaner.api.api.query;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum Slot {
  MAIN("main"),
  ;

  public final String value;

  Slot(final String value) {
    this.value = value;
  }
}
