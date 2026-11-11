package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;

public record CheckAlgorithmConfiguration(
    int number,
    Map<String, String> properties,
    Set<PageTitle> whitelist,
    @Nullable CheckAlgorithmParameters parameters) {

  private static final String PRIORITY = "prio";
  private static final String LINK = "link";
  private static final String LONG_DESCRIPTION = "desc";
  private static final String SHORT_DESCRIPTION = "head";
  private static final String WHITELIST = "whitelist";
  private static final String WHITELIST_PAGE = "whitelistpage";
  private static final int FIRST_ACTIVE_PRIORITY = 1;
  private static final int LAST_ACTIVE_PRIORITY = 4;
  private static final int BOT_ONLY_PRIORITY = 4;
  private static final String TEMPLATE_FIELD = "template";
  private static final String PARAMETERS_FIELD = "parameters";

  public CheckAlgorithmConfiguration {
    properties = Map.copyOf(properties);
    whitelist = Set.copyOf(whitelist);
  }

  public CheckAlgorithmConfiguration(final int number, final Map<String, String> properties) {
    this(number, properties, Set.of(), null);
  }

  public CheckAlgorithmConfiguration withWhitelist(final Set<PageTitle> newWhitelist) {
    return new CheckAlgorithmConfiguration(number, properties, newWhitelist, parameters);
  }

  public CheckAlgorithmConfiguration withParameters(
      @Nullable final CheckAlgorithmParameters newParameters) {
    return new CheckAlgorithmConfiguration(number, properties, whitelist, newParameters);
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

  public boolean getBoolean(final String name) {
    if (parameters != null) {
      return parameters.getBoolean(name).orElse(false);
    }
    return getProperty(name).map(Boolean::parseBoolean).orElse(false);
  }

  public List<String> getStrings(final String name) {
    if (parameters != null) {
      return parameters.getStrings(name);
    }
    return getLines(name).stream()
        .map(line -> ConfigurationParser.splitLine(line).getFirst())
        .toList();
  }

  public Set<String> getTemplateNames(
      final String templatesName, final String groupsName, final TemplateGroups groups) {
    if (parameters != null) {
      return Stream.concat(
              parameters.getStrings(templatesName).stream().map(String::trim),
              parameters.getStrings(groupsName).stream()
                  .flatMap(groupName -> groups.getGroupTemplateNames(groupName).stream()))
          .filter(name -> !name.isEmpty())
          .collect(Collectors.toSet());
    }
    return getStrings(templatesName).stream()
        .flatMap(names -> groups.getTemplateNames(names).stream())
        .collect(Collectors.toSet());
  }

  public TemplateParameters getTemplateParameters(
      final String templatesName, final String parametersName) {
    if (parameters != null) {
      return TemplateParameters.of(
          parameters.getObjects(templatesName).stream()
              .flatMap(object -> object.getStrings(TEMPLATE_FIELD).stream())
              .toList(),
          parameters.getObjects(parametersName).stream()
              .flatMap(
                  object ->
                      object.getStrings(TEMPLATE_FIELD).stream()
                          .map(
                              template -> Map.entry(template, object.getStrings(PARAMETERS_FIELD))))
              .toList());
    }
    final List<List<String>> lines =
        getLines(templatesName).stream().map(ConfigurationParser::splitLine).toList();
    return TemplateParameters.of(
        lines.stream().filter(line -> line.size() == 1).map(List::getFirst).toList(),
        lines.stream()
            .filter(line -> line.size() > 1)
            .map(line -> Map.entry(line.getFirst(), line.subList(1, line.size())))
            .toList());
  }

  public List<TemplateDefinition> getTemplateDefinitions(
      final String name, final List<String> fields) {
    if (parameters != null) {
      return parameters.getObjects(name).stream()
          .flatMap(
              object ->
                  object.getStrings(TEMPLATE_FIELD).stream()
                      .flatMap(
                          template ->
                              TemplateDefinition.of(
                                  template,
                                  fields,
                                  fields.stream()
                                      .map(
                                          field ->
                                              object.getStrings(field).stream()
                                                  .findFirst()
                                                  .orElse(""))
                                      .toList())
                                  .stream()))
          .toList();
    }
    return getLines(name).stream()
        .map(ConfigurationParser::splitLine)
        .flatMap(
            line ->
                TemplateDefinition.of(line.getFirst(), fields, line.subList(1, line.size()))
                    .stream())
        .toList();
  }

  public TemplateGroups getTemplateGroups(final String name) {
    if (parameters != null) {
      return TemplateGroups.of(parameters.getStringLists(name));
    }
    return TemplateGroups.of(getLines(name));
  }

  public List<String> getLines(final String name) {
    return getProperty(name).map(ConfigurationParser::toLines).orElse(List.of());
  }

  public Optional<String> getProperty(final String name) {
    return Optional.ofNullable(properties.get(name)).filter(value -> !value.isEmpty());
  }
}
