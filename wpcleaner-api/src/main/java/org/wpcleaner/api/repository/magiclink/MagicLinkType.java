package org.wpcleaner.api.repository.magiclink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Optional;

public enum MagicLinkType {
  ISBN,
  PMID,
  RFC;

  public static Optional<MagicLinkType> findByName(final String name) {
    return Arrays.stream(values()).filter(type -> type.name().equals(name)).findFirst();
  }
}
