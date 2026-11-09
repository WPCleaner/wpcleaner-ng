package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;

public record CheckAlgorithmConfiguration(
    int number, Map<String, String> properties, Set<PageTitle> whitelist) {

  private static final String PRIORITY = "prio";
  private static final String LINK = "link";
  private static final String LONG_DESCRIPTION = "desc";
  private static final String SHORT_DESCRIPTION = "head";
  private static final String WHITELIST = "whitelist";
  private static final String WHITELIST_PAGE = "whitelistpage";
  private static final int FIRST_ACTIVE_PRIORITY = 1;
  private static final int LAST_ACTIVE_PRIORITY = 4;
  private static final int BOT_ONLY_PRIORITY = 4;

  public CheckAlgorithmConfiguration {
    properties = Map.copyOf(properties);
    whitelist = Set.copyOf(whitelist);
  }

  public CheckAlgorithmConfiguration(final int number, final Map<String, String> properties) {
    this(number, properties, Set.of());
  }

  public CheckAlgorithmConfiguration withWhitelist(final Set<PageTitle> newWhitelist) {
    return new CheckAlgorithmConfiguration(number, properties, newWhitelist);
  }

  public boolean isActive() {
    return getPriority()
        .filter(priority -> FIRST_ACTIVE_PRIORITY <= priority && priority <= LAST_ACTIVE_PRIORITY)
        .isPresent();
  }

  public boolean isBotOnly() {
    return getPriority().filter(priority -> priority == BOT_ONLY_PRIORITY).isPresent();
  }

  private Optional<Integer> getPriority() {
    return getProperty(PRIORITY)
        .filter(priority -> priority.chars().allMatch(Character::isDigit))
        .map(Integer::parseInt);
  }

  public Optional<String> shortDescription() {
    return getProperty(SHORT_DESCRIPTION);
  }

  public Optional<String> longDescription() {
    return getProperty(LONG_DESCRIPTION);
  }

  public Optional<String> link() {
    return getProperty(LINK);
  }

  public Optional<String> whitelistPage() {
    return getProperty(WHITELIST_PAGE);
  }

  public List<String> inlineWhitelist() {
    return getLines(WHITELIST);
  }

  public boolean isWhitelisted(final PageTitle title) {
    return whitelist.contains(title);
  }

  public List<String> getLines(final String name) {
    return getProperty(name).map(ConfigurationParser::toLines).orElse(List.of());
  }

  public Optional<String> getProperty(final String name) {
    return Optional.ofNullable(properties.get(name)).filter(value -> !value.isEmpty());
  }
}
