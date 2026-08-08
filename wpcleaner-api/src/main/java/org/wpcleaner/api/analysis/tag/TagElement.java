package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;

public record TagElement(int begin, int end, String name) implements Element {

  public static final char TOKEN_START = '<';
  public static final char TOKEN_END = '>';
  public static final char TOKEN_ATTRIBUTE_VALUE = '=';
}
