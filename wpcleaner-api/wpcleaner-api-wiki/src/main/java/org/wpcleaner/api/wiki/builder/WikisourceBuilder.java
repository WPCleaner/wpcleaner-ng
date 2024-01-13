package org.wpcleaner.api.wiki.builder;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.ComponentOrientation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiGroup;
import org.wpcleaner.api.wiki.definition.WikiWarning;
import org.wpcleaner.lib.image.ImageCollection;

public class WikisourceBuilder {

  public static WikiDefinition ltr(final String language, final String name) {
    return build(language, name, ComponentOrientation.LEFT_TO_RIGHT, null);
  }

  public static WikiDefinition ltr(
      final String language, final String name, final WikiWarning warning) {
    return build(language, name, ComponentOrientation.LEFT_TO_RIGHT, warning);
  }

  public static WikiDefinition rtl(final String language, final String name) {
    return build(language, name, ComponentOrientation.RIGHT_TO_LEFT, null);
  }

  private static WikiDefinition build(
      final String language,
      final String name,
      final ComponentOrientation orientation,
      @Nullable final WikiWarning warning) {
    return new WikiBuilder(language, name, orientation)
        .withIcon(ImageCollection.LOGO_WIKISOURCE)
        .withGroup(WikiGroup.WIKIMEDIA)
        .withHost("%s.wikisource.org".formatted(language))
        .withCode("s:%s".formatted(language))
        .withCheckWikiCode("%swikisource".formatted(language))
        .withWarning(warning)
        .build();
  }
}
