package org.wpcleaner.api.analysis.internallink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;

@SuppressWarnings("PMD.DataClass")
public record InternalLinkElement(int begin, int end) implements Element {

  public static final char TOKEN_START = '[';
  public static final int START_COUNT = 2;
  public static final char TOKEN_END = ']';
  public static final int END_COUNT = 2;
  public static final char TOKEN_SEPARATOR = '|';
}
