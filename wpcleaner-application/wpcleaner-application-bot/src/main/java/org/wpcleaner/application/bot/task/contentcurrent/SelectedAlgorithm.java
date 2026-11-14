package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.wpcleaner.lib.check.CheckAlgorithm;

public record SelectedAlgorithm(CheckAlgorithm algorithm, Set<Integer> namespaces) {

  public SelectedAlgorithm {
    namespaces = Set.copyOf(namespaces);
  }
}
