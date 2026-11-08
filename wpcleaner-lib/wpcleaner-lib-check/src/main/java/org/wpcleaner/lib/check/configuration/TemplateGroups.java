package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;

public record TemplateGroups(Map<String, Set<String>> groups) {

  private static final String GROUP_PREFIX = "+";
  private static final Pattern NAMES_SEPARATOR = Pattern.compile(",");

  public TemplateGroups {
    groups = Map.copyOf(groups);
  }

  public static TemplateGroups of(final List<String> lines) {
    final Map<String, Set<String>> groups = new HashMap<>();
    for (final String line : lines) {
      final List<String> elements = ConfigurationParser.splitLine(line);
      final String groupName = elements.getFirst().trim();
      if (elements.size() > 1 && !groupName.isEmpty()) {
        elements.stream()
            .skip(1)
            .map(String::trim)
            .filter(name -> !name.isEmpty())
            .forEach(name -> groups.computeIfAbsent(groupName, _ -> new HashSet<>()).add(name));
      }
    }
    return new TemplateGroups(
        groups.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue()))));
  }

  public Set<String> getTemplateNames(final String names) {
    final Set<String> templateNames = new HashSet<>();
    final Set<String> addedGroups = new HashSet<>();
    for (final String name : NAMES_SEPARATOR.split(names, -1)) {
      addTemplateName(templateNames, name.trim(), addedGroups);
    }
    return Set.copyOf(templateNames);
  }

  private void addTemplateName(
      final Set<String> templateNames, final String name, final Set<String> addedGroups) {
    if (name.isEmpty()) {
      return;
    }
    if (!name.startsWith(GROUP_PREFIX)) {
      templateNames.add(name);
      return;
    }
    final String groupName = name.substring(GROUP_PREFIX.length()).trim();
    if (addedGroups.add(groupName)) {
      groups
          .getOrDefault(groupName, Set.of())
          .forEach(templateName -> addTemplateName(templateNames, templateName, addedGroups));
    }
  }
}
