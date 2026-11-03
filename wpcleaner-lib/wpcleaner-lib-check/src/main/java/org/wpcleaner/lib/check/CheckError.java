package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;

public record CheckError(int algorithm, int begin, int end, List<Replacement> replacements)
    implements Element {

  public CheckError {
    replacements = List.copyOf(replacements);
  }
}
