package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record Replacement(String text, String label, boolean automatic) {

  public Replacement(final String text, final boolean automatic) {
    this(text, text, automatic);
  }
}
