package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record EditQueryByPageId(Integer pageId, EditQueryCommon common) implements EditQuery {
  @Override
  public EditQuery withCommon(final EditQueryCommon common) {
    return new EditQueryByPageId(pageId, common);
  }

  @Override
  public String description() {
    return "id %s".formatted(pageId);
  }
}
