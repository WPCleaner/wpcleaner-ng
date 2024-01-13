package org.wpcleaner.api.wiki.builder;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.ComponentOrientation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiGroup;
import org.wpcleaner.lib.image.ImageCollection;

public class WikiquoteBuilder {

  public static WikiDefinition ltr(final String language, final String name) {
    return build(language, name, ComponentOrientation.LEFT_TO_RIGHT);
  }

  public static WikiDefinition rtl(final String language, final String name) {
    return build(language, name, ComponentOrientation.RIGHT_TO_LEFT);
  }

  private static WikiDefinition build(
      final String language, final String name, final ComponentOrientation orientation) {
    return new WikiBuilder(language, name, orientation)
        .withIcon(ImageCollection.LOGO_WIKIQUOTE)
        .withGroup(WikiGroup.WIKIMEDIA)
        .withHost("%s.wikiquote.org".formatted(language))
        .withCode("q:%s".formatted(language))
        .withCheckWikiCode("%swikiquote".formatted(language))
        .build();
  }
}
