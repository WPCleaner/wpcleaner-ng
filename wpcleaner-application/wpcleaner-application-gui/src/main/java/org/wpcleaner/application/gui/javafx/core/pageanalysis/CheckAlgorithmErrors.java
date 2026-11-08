package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;

public record CheckAlgorithmErrors(CheckAlgorithm algorithm, String name, List<CheckError> errors) {

  public CheckAlgorithmErrors {
    errors = List.copyOf(errors);
  }
}
