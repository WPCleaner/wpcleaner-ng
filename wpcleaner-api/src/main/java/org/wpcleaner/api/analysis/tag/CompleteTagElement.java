package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.Element;

public record CompleteTagElement(TagElement startTag, TagElement endTag) implements Element {

  @Override
  public int begin() {
    return startTag.begin();
  }

  @Override
  public int end() {
    return endTag.end();
  }
}
