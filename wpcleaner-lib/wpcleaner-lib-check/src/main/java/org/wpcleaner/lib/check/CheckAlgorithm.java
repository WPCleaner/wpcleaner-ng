package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.utils.GT;

public interface CheckAlgorithm {

  int number();

  default String name() {
    return GT._T("Error No. %d", number());
  }

  List<CheckError> analyze(PageAnalysis analysis);
}
