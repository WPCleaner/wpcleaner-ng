package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.FandomBuilder;

@Component
@SuppressWarnings("unused")
final class FandomDefinitions implements WikiDefinitions {

  static final WikiDefinition DC = FandomBuilder.build("dc", "DC Database");
  static final WikiDefinition DISNEY = FandomBuilder.build("disney", "Disney Wiki");
  static final WikiDefinition DISNEY_FR =
      FandomBuilder.build("disney", "fr", "Disney Wiki (French)");
  static final WikiDefinition FALLOUT = FandomBuilder.build("fallout", "Fallout Wiki");
  static final WikiDefinition HARRYPOTTER = FandomBuilder.build("harrypotter", "Harry Potter Wiki");
  static final WikiDefinition MARVEL = FandomBuilder.build("marvel", "Marvel Database");
  static final WikiDefinition MEMORY_ALPHA = FandomBuilder.build("memory-alpha", "Memory Alpha");
  static final WikiDefinition MINECRAFT = FandomBuilder.build("minecraft", "Minecraft Wiki");
  static final WikiDefinition STARWARS = FandomBuilder.build("starwars", "Wookieepedia");

  private FandomDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
