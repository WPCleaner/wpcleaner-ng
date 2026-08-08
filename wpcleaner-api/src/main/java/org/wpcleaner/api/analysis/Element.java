package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;

public interface Element {

  Comparator<Element> COMPARATOR =
      Comparator.comparingInt(Element::begin).thenComparingInt(Element::end);

  int begin();

  int end();
}
