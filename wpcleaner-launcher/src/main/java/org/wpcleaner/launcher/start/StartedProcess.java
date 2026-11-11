package org.wpcleaner.launcher.start;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.util.OptionalInt;

@FunctionalInterface
public interface StartedProcess {

  OptionalInt exitCode(Duration timeout) throws InterruptedException;
}
