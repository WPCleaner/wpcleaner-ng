package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public record CheckAlgorithmParameters(Map<String, Object> values) {

  public static final CheckAlgorithmParameters EMPTY = new CheckAlgorithmParameters(Map.of());

  public CheckAlgorithmParameters {
    values = Map.copyOf(values);
  }

  public static CheckAlgorithmParameters parse(final String yaml) {
    final Object document;
    try {
      document = new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
    } catch (final YAMLException e) {
      throw new IllegalArgumentException(
          GT._T("Invalid YAML: %s", String.valueOf(e.getMessage())), e);
    }
    if (document == null) {
      return EMPTY;
    }
    if (!(document instanceof Map<?, ?> mapping)) {
      throw new IllegalArgumentException(GT._T("The YAML configuration must be a mapping"));
    }
    final Map<String, Object> values = new LinkedHashMap<>();
    mapping.forEach(
        (name, value) -> {
          if (name != null && value != null) {
            values.put(name.toString(), value);
          }
        });
    return new CheckAlgorithmParameters(values);
  }

  public Optional<Boolean> getBoolean(final String name) {
    return Optional.ofNullable(values.get(name))
        .filter(Boolean.class::isInstance)
        .map(Boolean.class::cast);
  }

  public List<String> getStrings(final String name) {
    return toStrings(values.get(name));
  }

  public Map<String, List<String>> getStringLists(final String name) {
    if (!(values.get(name) instanceof Map<?, ?> mapping)) {
      return Map.of();
    }
    final Map<String, List<String>> stringLists = new LinkedHashMap<>();
    mapping.forEach(
        (key, value) -> {
          if (key != null) {
            stringLists.put(key.toString(), toStrings(value));
          }
        });
    return Map.copyOf(stringLists);
  }

  private static List<String> toStrings(@Nullable final Object value) {
    final Stream<?> elements =
        value instanceof List<?> list ? list.stream() : Stream.ofNullable(value);
    return elements
        .filter(Objects::nonNull)
        .filter(element -> !(element instanceof Map<?, ?> || element instanceof List<?>))
        .map(Object::toString)
        .toList();
  }
}
