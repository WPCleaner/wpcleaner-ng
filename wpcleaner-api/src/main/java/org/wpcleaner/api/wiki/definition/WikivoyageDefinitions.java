package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.WikivoyageBuilder;

@Component
@Order(0)
@SuppressWarnings("unused")
final class WikivoyageDefinitions implements WikiDefinitions {

  public static final WikiDefinition BN =
      WikivoyageBuilder.ltr("bn", "Bengali Wikivoyage", WikiWarning.BENGALI);
  public static final WikiDefinition FR = WikivoyageBuilder.ltr("fr", "Wikivoyage en français");

  private WikivoyageDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
