package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.WiktionaryBuilder;

@Component
@Order(0)
@SuppressWarnings({"SpellCheckingInspection", "unused"})
final class WiktionaryDefinitions implements WikiDefinitions {

  static final WikiDefinition BN =
      WiktionaryBuilder.ltr("bn", "Bengali Wiktionary", WikiWarning.BENGALI);
  static final WikiDefinition EN = WiktionaryBuilder.ltr("en", "English Wiktionary");
  static final WikiDefinition FR = WiktionaryBuilder.ltr("fr", "Wiktionnaire en français");
  static final WikiDefinition PT = WiktionaryBuilder.ltr("pt", "Portuguese Wiktionary");
  static final WikiDefinition SV = WiktionaryBuilder.ltr("sv", "Swedish Wiktionary");

  private WiktionaryDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
