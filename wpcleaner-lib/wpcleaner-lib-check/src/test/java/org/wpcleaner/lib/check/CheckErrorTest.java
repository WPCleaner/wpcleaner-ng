package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckErrorTest {

  @DisplayName("should keep only the replacements among the suggestions, in their order")
  @Test
  void replacements() {
    // GIVEN
    final Replacement first = new Replacement("a", true);
    final Replacement second = new Replacement("b", "B", false);
    final CheckError error =
        new CheckError(1, 0, 5, List.of(first, new ExpandedReplacement("{{X}}", "Expand"), second));

    // WHEN
    final List<Replacement> replacements = error.replacements();

    // THEN
    Assertions.assertThat(replacements).containsExactly(first, second);
  }
}
