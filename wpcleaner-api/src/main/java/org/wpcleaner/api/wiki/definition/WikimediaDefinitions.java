package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.lib.image.ImageCollection;

@Service
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@SuppressWarnings("unused")
public class WikimediaDefinitions implements WikiDefinitions {
  public static final WikiDefinition COMMONS =
      WikiBuilder.ltr("en", "Commons")
          .withIcon(ImageCollection.LOGO_COMMONS)
          .withGroup(WikiGroup.WIKIMEDIA)
          .withHost("commons.wikimedia.org")
          .withCode("commons")
          .withCheckWikiCode("commons")
          .build();
  public static final WikiDefinition META =
      WikiBuilder.ltr("en", "Meta")
          .withIcon(ImageCollection.LOGO_WIKIMEDIA)
          .withGroup(WikiGroup.WIKIMEDIA)
          .withHost("meta.wikimedia.org")
          .withCode("meta")
          .withCheckWikiCode("meta")
          .build();
}
