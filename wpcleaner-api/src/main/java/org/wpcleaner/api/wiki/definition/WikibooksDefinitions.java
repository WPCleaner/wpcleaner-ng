package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.WikibooksBuilder;

@Component
@Order(0)
final class WikibooksDefinitions implements WikiDefinitions {
  public static final WikiDefinition BN =
      WikibooksBuilder.ltr("bn", "Bengali Wikibooks", WikiWarning.BENGALI);

  private WikibooksDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
