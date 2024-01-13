package org.wpcleaner.api.wiki.builder;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.ComponentOrientation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiGroup;
import org.wpcleaner.lib.image.ImageCollection;

public class WikiversityBuilder {

  public static WikiDefinition ltr(final String language, final String name) {
    return build(language, name, ComponentOrientation.LEFT_TO_RIGHT);
  }

  public static WikiDefinition rtl(final String language, final String name) {
    return build(language, name, ComponentOrientation.RIGHT_TO_LEFT);
  }

  private static WikiDefinition build(
      final String language, final String name, final ComponentOrientation orientation) {
    return new WikiBuilder(language, name, orientation)
        .withIcon(ImageCollection.LOGO_WIKIVERSITY)
        .withGroup(WikiGroup.WIKIMEDIA)
        .withHost("%s.wikiversity.org".formatted(language))
        .withCode("v:%s".formatted(language))
        .withCheckWikiCode("%swikiversity".formatted(language))
        .build();
  }
}
