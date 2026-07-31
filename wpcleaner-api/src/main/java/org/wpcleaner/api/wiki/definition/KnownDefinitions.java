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
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.settings.GeneralSettingsManager;
import org.wpcleaner.api.settings.SettingsPersistence;

@Service
public class KnownDefinitions {

  private final SettingsPersistence persistence;
  private final List<WikiDefinition> definitions;
  private final Set<WikiDefinition> userAddedDefinitions =
      new TreeSet<>(Comparator.comparing(WikiDefinition::code));
  private final WikiDefinition preferredWiki;

  public KnownDefinitions(
      final List<WikiDefinitions> wikiDefinitions,
      final GeneralSettingsManager generalSettings,
      final SettingsPersistence persistence) {
    this.persistence = persistence;
    definitions =
        new ArrayList<>(
            wikiDefinitions.stream()
                .map(WikiDefinitions::getDefinitions)
                .map(this::convertSetToOrderedList)
                .flatMap(Collection::stream)
                .toList());

    final UserWikisSettings userWikisSettings =
        persistence.load(UserWikisSettings.class).orElseGet(UserWikisSettings::new);
    userAddedDefinitions.addAll(userWikisSettings.wikis());
    definitions.addAll(userAddedDefinitions);

    preferredWiki =
        Optional.ofNullable(generalSettings.getCurrentSettings().preferredWiki())
            .flatMap(preferred -> WikiDefinitionHelper.findByCode(definitions, preferred))
            .orElse(WikipediaDefinitions.EN);
  }

  public void addDefinition(final WikiDefinition definition) {
    if (!definitions.contains(definition)) {
      definitions.add(definition);
      userAddedDefinitions.add(definition);
      saveUserWikis();
    }
  }

  public boolean isUserAdded(final WikiDefinition definition) {
    return userAddedDefinitions.contains(definition);
  }

  public void removeDefinition(final WikiDefinition definition) {
    definitions.remove(definition);
    userAddedDefinitions.remove(definition);
    saveUserWikis();
  }

  private void saveUserWikis() {
    persistence.save(
        new UserWikisSettings(UserWikisSettings.LAST_VERSION, List.copyOf(userAddedDefinitions)));
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
