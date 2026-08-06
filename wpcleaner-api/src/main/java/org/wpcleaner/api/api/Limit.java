package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public sealed interface Limit permits Limit.Numeric, Limit.Max {

  String value();

  static Numeric of(final int limit) {
    return new Numeric(limit);
  }

  static Max max() {
    return Max.INSTANCE;
  }

  record Numeric(int limit) implements Limit {
    @Override
    public String value() {
      return Integer.toString(limit);
    }
  }

  record Max() implements Limit {
    private static final Max INSTANCE = new Max();

    @Override
    public String value() {
      return "max";
    }
  }
}
