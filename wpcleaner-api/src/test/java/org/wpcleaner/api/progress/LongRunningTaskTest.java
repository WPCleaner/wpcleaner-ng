package org.wpcleaner.api.progress;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LongRunningTaskTest {

  @DisplayName("LongRunningTask showProgress defaults to true")
  @Test
  void testShowProgressDefaultIsTrue() {
    final LongRunningTask<String> task = _ -> "result";
    Assertions.assertThat(task.showProgress()).isTrue();
  }
}
