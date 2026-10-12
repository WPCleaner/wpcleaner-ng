package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record MoveQueryByTitle(String from, MoveQueryCommon common) implements MoveQuery {

  @Override
  public String description() {
    return "from %s to %s".formatted(from, common.to());
  }

  @Override
  public MoveQuery withCommon(final MoveQueryCommon common) {
    return new MoveQueryByTitle(from, common);
  }
}
