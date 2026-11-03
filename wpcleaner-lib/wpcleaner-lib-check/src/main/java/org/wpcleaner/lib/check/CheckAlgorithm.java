package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.PageAnalysis;

public interface CheckAlgorithm {

  int number();

  List<CheckError> analyze(PageAnalysis analysis);
}
