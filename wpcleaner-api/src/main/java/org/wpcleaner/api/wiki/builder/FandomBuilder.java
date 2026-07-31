package org.wpcleaner.api.wiki.builder;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.ComponentOrientation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiGroup;
import org.wpcleaner.lib.image.ImageCollection;

public final class FandomBuilder {

  private FandomBuilder() {
    // Utility class
  }

  public static WikiDefinition build(final String subdomain, final String name) {
    return new WikiBuilder(
            "en", name, "%s.fandom.com".formatted(subdomain), ComponentOrientation.LEFT_TO_RIGHT)
        .withIcon(ImageCollection.LOGO_FANDOM)
        .withGroup(WikiGroup.FANDOM)
        .withApiPath("/api.php")
        .withIndexPath("/index.php")
        .withWikiPath("/wiki")
        .withCode("fandom:%s".formatted(subdomain))
        .build();
  }

  public static WikiDefinition build(
      final String subdomain, final String language, final String name) {
    return new WikiBuilder(
            language,
            name,
            "%s.fandom.com".formatted(subdomain),
            ComponentOrientation.LEFT_TO_RIGHT)
        .withIcon(ImageCollection.LOGO_FANDOM)
        .withGroup(WikiGroup.FANDOM)
        .withApiPath("/%s/api.php".formatted(language))
        .withIndexPath("/%s/index.php".formatted(language))
        .withWikiPath("/%s/wiki".formatted(language))
        .withCode("fandom:%s:%s".formatted(subdomain, language))
        .build();
  }
}
