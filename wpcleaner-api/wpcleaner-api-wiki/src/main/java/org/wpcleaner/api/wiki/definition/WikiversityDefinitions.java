package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikiversityBuilder;

@Service
@Order(0)
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class WikiversityDefinitions implements WikiDefinitions {
  public static final WikiDefinition FR = WikiversityBuilder.ltr("fr", "Wikiversité en Français");
  public static final WikiDefinition IT = WikiversityBuilder.ltr("it", "Wikiversità");
}
