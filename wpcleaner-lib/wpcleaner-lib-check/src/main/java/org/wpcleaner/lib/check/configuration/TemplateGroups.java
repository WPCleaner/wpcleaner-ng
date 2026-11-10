package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;

public record TemplateGroups(Map<String, Set<String>> groups) {

  private static final String GROUP_PREFIX = "+";
  private static final Pattern NAMES_SEPARATOR = Pattern.compile(",");

  public TemplateGroups {
    groups =
        groups.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
  }

  public static TemplateGroups of(final List<String> lines) {
    final Map<String, List<String>> namesByGroup = new HashMap<>();
    for (final String line : lines) {
      final List<String> elements = ConfigurationParser.splitLine(line);
      namesByGroup
          .computeIfAbsent(elements.getFirst(), _ -> new ArrayList<>())
          .addAll(elements.subList(1, elements.size()));
    }
    return of(namesByGroup);
  }

  public static TemplateGroups of(final Map<String, List<String>> namesByGroup) {
    final Map<String, Set<String>> groups = new HashMap<>();
    namesByGroup.forEach((groupName, names) -> addNames(groups, groupName.trim(), names));
    return new TemplateGroups(groups);
  }

  public TemplateGroups merge(final TemplateGroups other) {
    final Map<String, Set<String>> mergedGroups = new HashMap<>();
    Stream.of(groups, other.groups)
        .forEach(
            groupsToMerge ->
                groupsToMerge.forEach(
                    (groupName, names) -> addNames(mergedGroups, groupName, names)));
    return new TemplateGroups(mergedGroups);
  }

  private static void addNames(
      final Map<String, Set<String>> groups,
      final String groupName,
      final Collection<String> names) {
    if (groupName.isEmpty()) {
      return;
    }
    names.stream()
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .forEach(name -> groups.computeIfAbsent(groupName, _ -> new HashSet<>()).add(name));
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
    addGroupTemplateNames(templateNames, name.substring(GROUP_PREFIX.length()).trim(), addedGroups);
  }

  public Set<String> getGroupTemplateNames(final String groupName) {
    final Set<String> templateNames = new HashSet<>();
    addGroupTemplateNames(templateNames, groupName.trim(), new HashSet<>());
    return Set.copyOf(templateNames);
  }

  private void addGroupTemplateNames(
      final Set<String> templateNames, final String groupName, final Set<String> addedGroups) {
    if (addedGroups.add(groupName)) {
      groups
          .getOrDefault(groupName, Set.of())
          .forEach(templateName -> addTemplateName(templateNames, templateName, addedGroups));
    }
  }
}
