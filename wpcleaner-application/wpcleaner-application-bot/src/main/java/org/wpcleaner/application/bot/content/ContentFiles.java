package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import java.util.List;

public record ContentFiles(String wiki, String date, List<Path> files) {

  public ContentFiles {
    files = List.copyOf(files);
  }

  public String name() {
    return "%s-%s".formatted(wiki, date);
  }
}
