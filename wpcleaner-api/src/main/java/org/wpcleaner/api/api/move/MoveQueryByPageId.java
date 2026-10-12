package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record MoveQueryByPageId(Integer fromId, MoveQueryCommon common) implements MoveQuery {

  @Override
  public String description() {
    return "id %s to %s".formatted(fromId, common.to());
  }

  @Override
  public MoveQuery withCommon(final MoveQueryCommon common) {
    return new MoveQueryByPageId(fromId, common);
  }
}
