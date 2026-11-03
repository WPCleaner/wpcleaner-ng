package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CheckSpringBootTestConfiguration.class)
class CheckAlgorithmRegistryTest {

  @Autowired private CheckAlgorithmRegistry registry;

  @DisplayName("should find the algorithms by their number")
  @Test
  void findAlgorithm() {
    // GIVEN
    final int number = 48;

    // WHEN
    final Optional<CheckAlgorithm> algorithm = registry.findAlgorithm(number);

    // THEN
    Assertions.assertThat(algorithm).map(CheckAlgorithm::number).contains(number);
    Assertions.assertThat(registry.findAlgorithm(0)).isEmpty();
  }
}
