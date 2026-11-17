package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;
import java.util.Optional;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;

record EnabledControlCharacters(CheckAlgorithmConfiguration configuration) {

  private static final String PARAMETER_USE_PREFIX = "use_";

  Optional<ControlCharacter> find(final int codePoint) {
    return ControlCharacter.of(codePoint)
        .filter(
            control ->
                configuration.getBoolean(
                    PARAMETER_USE_PREFIX + Integer.toHexString(codePoint).toUpperCase(Locale.ROOT),
                    true));
  }
}
