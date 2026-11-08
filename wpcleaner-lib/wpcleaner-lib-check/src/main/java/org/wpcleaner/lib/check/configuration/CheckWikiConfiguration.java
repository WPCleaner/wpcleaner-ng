package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;

public record CheckWikiConfiguration(Map<Integer, CheckAlgorithmConfiguration> algorithms) {

  public static final CheckWikiConfiguration EMPTY = new CheckWikiConfiguration(Map.of());

  public CheckWikiConfiguration {
    algorithms = Map.copyOf(algorithms);
  }

  public static CheckWikiConfiguration of(
      final Map<String, String> wikiProperties,
      final Map<String, String> userProperties,
      final String code) {
    final Pattern propertyPattern = Pattern.compile("error_(\\d{3})_(.+)_" + Pattern.quote(code));
    final Map<Integer, Map<String, String>> propertiesByNumber = new HashMap<>();
    Stream.of(wikiProperties, userProperties)
        .forEach(
            properties ->
                properties.forEach(
                    (name, value) -> {
                      final Matcher matcher = propertyPattern.matcher(name);
                      final int number = matcher.matches() ? Integer.parseInt(matcher.group(1)) : 0;
                      if (number > 0) {
                        propertiesByNumber
                            .computeIfAbsent(number, _ -> new HashMap<>())
                            .put(matcher.group(2), value);
                      }
                    }));
    return new CheckWikiConfiguration(
        propertiesByNumber.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new CheckAlgorithmConfiguration(entry.getKey(), entry.getValue()))));
  }

  public CheckWikiConfiguration withWhitelists(
      final Function<CheckAlgorithmConfiguration, Set<PageTitle>> whitelists) {
    return new CheckWikiConfiguration(
        algorithms.values().stream()
            .collect(
                Collectors.toMap(
                    CheckAlgorithmConfiguration::number,
                    algorithm -> algorithm.withWhitelist(whitelists.apply(algorithm)))));
  }

  public Optional<CheckAlgorithmConfiguration> getAlgorithm(final int number) {
    return Optional.ofNullable(algorithms.get(number));
  }
}
