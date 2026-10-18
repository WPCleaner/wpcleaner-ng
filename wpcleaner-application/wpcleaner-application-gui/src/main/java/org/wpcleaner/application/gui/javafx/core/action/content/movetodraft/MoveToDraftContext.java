package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record MoveToDraftContext(
    String blockTitle,
    WikiDefinition wiki,
    String basePageTitle,
    String subPageTitle,
    String pageContent) {

  public MoveToDraftContext {
    Objects.requireNonNull(blockTitle, "blockTitle");
    Objects.requireNonNull(wiki, "wiki");
    Objects.requireNonNull(basePageTitle, "basePageTitle");
    Objects.requireNonNull(subPageTitle, "subPageTitle");
    Objects.requireNonNull(pageContent, "pageContent");
  }

  public String subPageFullTitle() {
    return subPageFullTitle(subPageTitle);
  }

  public String subPageFullTitle(final String subPage) {
    Objects.requireNonNull(subPage, "subPage");
    return basePageTitle + "/" + subPage.trim();
  }
}
