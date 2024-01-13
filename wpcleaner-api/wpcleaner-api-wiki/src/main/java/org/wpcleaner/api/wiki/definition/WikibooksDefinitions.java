package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikibooksBuilder;

@Service
@Order(0)
@SuppressWarnings("SpellCheckingInspection")
public class WikibooksDefinitions implements WikiDefinitions {
  public static final WikiDefinition BN =
      WikibooksBuilder.ltr("bn", "Bengali Wikibooks", WikiWarning.BENGALI);
}
