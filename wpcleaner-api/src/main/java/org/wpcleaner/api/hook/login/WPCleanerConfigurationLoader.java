package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class WPCleanerConfigurationLoader {

  public static final String GENERAL_USERNAME = "NicoV";

  private final ConfigurationPageReader pageReader;
  private final WPCleanerConfigurationRepository repository;

  public WPCleanerConfigurationLoader(
      final ConfigurationPageReader pageReader, final WPCleanerConfigurationRepository repository) {
    this.pageReader = pageReader;
    this.repository = repository;
  }

  public static String getConfigurationPage(final String username) {
    return "User:%s/WikiCleanerConfiguration".formatted(username);
  }

  public void load(final WikiDefinition wiki, final String username) {
    final String generalPage = getConfigurationPage(GENERAL_USERNAME);
    final String userPage = getConfigurationPage(username);
    repository.setConfiguration(
        wiki,
        new WPCleanerConfiguration(
            pageReader.retrieveProperties(wiki, generalPage),
            userPage.equals(generalPage)
                ? Map.of()
                : pageReader.retrieveProperties(wiki, userPage)));
  }
}
