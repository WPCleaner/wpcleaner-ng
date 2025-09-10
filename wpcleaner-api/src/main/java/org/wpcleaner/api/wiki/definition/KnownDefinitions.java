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
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.settings.local.general.GeneralSettingsManager;

@Service
public class KnownDefinitions {

  private final List<WikiDefinition> definitions;
  private final WikiDefinition preferredWiki;

  public KnownDefinitions(
      final List<WikiDefinitions> wikiDefinitions, final GeneralSettingsManager generalSettings) {
    definitions =
        wikiDefinitions.stream()
            .map(WikiDefinitions::getDefinitions)
            .map(this::convertSetToOrderedList)
            .flatMap(Collection::stream)
            .toList();
    preferredWiki =
        Optional.ofNullable(generalSettings.getCurrentSettings().preferredWiki())
            .flatMap(preferred -> WikiDefinitionHelper.findByCode(definitions, preferred))
            .orElse(WikipediaDefinitions.EN);
  }

  public List<WikiDefinition> getDefinitions() {
    return definitions;
  }

  public WikiDefinition getPreferred() {
    return preferredWiki;
  }

  public Optional<WikiDefinition> getDefinition(final String code) {
    return definitions.stream()
        .filter(definition -> Objects.equals(code, definition.code()))
        .findFirst();
  }

  private List<WikiDefinition> convertSetToOrderedList(final Set<WikiDefinition> set) {
    final List<WikiDefinition> result = new ArrayList<>(set);
    result.sort(Comparator.comparing(WikiDefinition::code));
    return result;
  }
}
