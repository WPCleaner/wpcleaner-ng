package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CheckAlgorithmRegistry {

  private final List<CheckAlgorithm> algorithms;

  public CheckAlgorithmRegistry(final List<CheckAlgorithm> algorithms) {
    this.algorithms =
        algorithms.stream().sorted(Comparator.comparingInt(CheckAlgorithm::number)).toList();
  }

  public List<CheckAlgorithm> getAlgorithms() {
    return algorithms;
  }

  public Optional<CheckAlgorithm> findAlgorithm(final int number) {
    return algorithms.stream().filter(algorithm -> algorithm.number() == number).findFirst();
  }
}
