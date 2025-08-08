package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikiquoteBuilder;

@Service
@Order(0)
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class WikiquoteDefinitions implements WikiDefinitions {
  public static final WikiDefinition CA = WikiquoteBuilder.ltr("ca", "Viquidites");
  public static final WikiDefinition FR = WikiquoteBuilder.ltr("fr", "Wikiquote en français");
  public static final WikiDefinition IT = WikiquoteBuilder.ltr("it", "Wikiquote");
}
