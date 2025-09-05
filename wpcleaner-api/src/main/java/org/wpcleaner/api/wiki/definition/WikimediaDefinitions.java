package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.lib.image.ImageCollection;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@SuppressWarnings("unused")
public final class WikimediaDefinitions implements WikiDefinitions {
  public static final WikiDefinition COMMONS =
      WikiBuilder.ltr("en", "Commons", "commons.wikimedia.org")
          .withIcon(ImageCollection.LOGO_COMMONS)
          .withGroup(WikiGroup.WIKIMEDIA)
          .withCode("commons")
          .withCheckWikiCode("commons")
          .build();
  public static final WikiDefinition MEDIAWIKI =
      WikiBuilder.ltr("en", "MediaWiki", "mediawiki.org")
          .withIcon(ImageCollection.LOGO_WIKIMEDIA)
          .withGroup(WikiGroup.WIKIMEDIA)
          .withCode("mw")
          .withCheckWikiCode("mw")
          .build();
  public static final WikiDefinition META =
      WikiBuilder.ltr("en", "Meta", "meta.wikimedia.org")
          .withIcon(ImageCollection.LOGO_WIKIMEDIA)
          .withGroup(WikiGroup.WIKIMEDIA)
          .withCode("meta")
          .withCheckWikiCode("meta")
          .build();

  private WikimediaDefinitions() {
    // Class defining only constants discovered by introspection
  }
}
