package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WiktionaryBuilder;

@Service
@Order(0)
@SuppressWarnings({"PMD.DataClass", "SpellCheckingInspection", "unused"})
public class WiktionaryDefinitions implements WikiDefinitions {
  public static final WikiDefinition BN =
      WiktionaryBuilder.ltr("bn", "Bengali Wiktionary", WikiWarning.BENGALI);
  public static final WikiDefinition EN = WiktionaryBuilder.ltr("en", "English Wiktionary");
  public static final WikiDefinition FR = WiktionaryBuilder.ltr("fr", "Wiktionnaire en français");
  public static final WikiDefinition SV = WiktionaryBuilder.ltr("sv", "Swedish Wiktionary");
}
