package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.WikisourceBuilder;

@Component
@Order(0)
@SuppressWarnings({"SpellCheckingInspection", "unused"})
final class WikisourceDefinitions implements WikiDefinitions {
  public static final WikiDefinition BN =
      WikisourceBuilder.ltr("bn", "Bengali Wikisource", WikiWarning.BENGALI);
  public static final WikiDefinition ES = WikisourceBuilder.ltr("es", "Wikisource en español");
  public static final WikiDefinition FR = WikisourceBuilder.ltr("fr", "Wikisource en français");

  private WikisourceDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
