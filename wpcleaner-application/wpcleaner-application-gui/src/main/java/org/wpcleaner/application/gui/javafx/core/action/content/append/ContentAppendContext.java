package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record ContentAppendContext(
    String blockTitle, WikiDefinition wiki, String pageTitle, String pageContent) {

  public ContentAppendContext {
    Objects.requireNonNull(blockTitle, "blockTitle");
    Objects.requireNonNull(wiki, "wiki");
    Objects.requireNonNull(pageTitle, "pageTitle");
    Objects.requireNonNull(Objects.requireNonNull(pageContent, "pageContent"));
  }
}
