package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KnownDefinitions {

  private final List<WikiDefinition> definitions;
  private final WikiDefinition preferredWiki;

  public KnownDefinitions(
      final List<WikiDefinitions> wikiDefinitions,
      @Value("${wpcleaner.wiki.preferred:}") final String preferredCode) {
    definitions =
        wikiDefinitions.stream()
            .map(WikiDefinitions::getDefinitions)
            .map(ArrayList::new)
            .peek(list -> list.sort(Comparator.comparing(WikiDefinition::code)))
            .flatMap(Collection::stream)
            .toList();
    preferredWiki =
        definitions.stream()
            .filter(definition -> Objects.equals(preferredCode, definition.code()))
            .findFirst()
            .orElse(WikipediaDefinitions.EN);
  }

  public List<WikiDefinition> getDefinitions() {
    return definitions;
  }

  public WikiDefinition getPreferred() {
    return preferredWiki;
  }
}
