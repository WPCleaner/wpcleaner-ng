package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.api.wiki.builder.WikipediaBuilder;

@Service
@SuppressWarnings({"PMD.DataClass", "unused"})
public class OtherDefinitions implements WikiDefinitions {
  public static final WikiDefinition WAZE =
      WikiBuilder.ltr("en", "Waze")
          .withHost("wiki.waze.com")
          .withApiPath("/wiki/api.php")
          .withIndexPath("/wiki/index.php")
          .withCode("waze")
          .build();
  public static final WikiDefinition WAZEOPEDIA_USA =
      WikiBuilder.ltr("en", "Wazeopedia USA")
          .withHost("wazeopedia.waze.com")
          .withApiPath("/wiki/USA/api.php")
          .withIndexPath("/wiki/USA/index.php")
          .withCode("wazeopediaUSA")
          .build();
  public static final WikiDefinition WIKISKRIPTA =
      WikiBuilder.ltr("cs", "WikiSkripta")
          .withHost("www.wikiskripta.eu")
          .withApiPath("/api.php")
          .withIndexPath("/index.php")
          .withCode("wikiskripta")
          .build();

  public static final WikiDefinition TEST = WikipediaBuilder.ltr("test", "Test wikipedia");
}
