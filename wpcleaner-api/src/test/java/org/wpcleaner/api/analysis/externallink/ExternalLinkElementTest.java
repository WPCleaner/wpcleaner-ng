package org.wpcleaner.api.analysis.externallink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExternalLinkElementTest {

  @DisplayName("Should create ExternalLinkElement with correct begin and end positions")
  @Test
  void createExternalLinkElement() {
    // GIVEN
    final int begin = 10;
    final int end = 25;

    // WHEN
    final ExternalLinkElement element = new ExternalLinkElement(begin, end);

    // THEN
    Assertions.assertThat(element.begin()).isEqualTo(begin);
    Assertions.assertThat(element.end()).isEqualTo(end);
  }
}
