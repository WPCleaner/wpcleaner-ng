package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.hook.login.ConfigurationPageReader;
import org.wpcleaner.api.hook.login.LoginStep;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckWikiConfigurationLoader implements LoginStep {

  private final ConfigurationPageReader pageReader;
  private final CheckWikiConfigurationRepository repository;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckWikiConfigurationLoader(
      final ConfigurationPageReader pageReader,
      final CheckWikiConfigurationRepository repository,
      final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.pageReader = pageReader;
    this.repository = repository;
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public void execute(final WikiDefinition wiki, final ProgressTracker tracker) {
    final WPCleanerConfiguration wpcleanerConfiguration =
        wpcleanerRepository.getConfiguration(wiki);
    final String code = wiki.checkWikiCode();
    final Optional<String> translationPage =
        wpcleanerConfiguration.getString(WPCleanerParameter.CHECK_WIKI_TRANSLATION_PAGE);
    if (code == null || translationPage.isEmpty()) {
      repository.setConfiguration(wiki, CheckWikiConfiguration.EMPTY);
      return;
    }
    try (ProgressStep _ = tracker.start(GT._T("Retrieving Check Wiki configuration"))) {
      repository.setConfiguration(
          wiki,
          CheckWikiConfiguration.of(
              pageReader.retrieveProperties(wiki, translationPage.get()),
              wpcleanerConfiguration.userProperties(),
              code));
    }
  }
}
