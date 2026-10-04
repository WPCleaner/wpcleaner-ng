package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record EditQueryByTitle(String title, EditQueryCommon common) implements EditQuery {
  @Override
  public EditQuery withCommon(final EditQueryCommon common) {
    return new EditQueryByTitle(title, common);
  }

  @Override
  public String description() {
    return "title %s".formatted(title);
  }
}
