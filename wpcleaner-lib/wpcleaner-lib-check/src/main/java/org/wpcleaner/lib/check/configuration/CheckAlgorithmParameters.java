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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

public record CheckAlgorithmParameters(Map<String, Object> values) {

  public static final CheckAlgorithmParameters EMPTY = new CheckAlgorithmParameters(Map.of());

  public CheckAlgorithmParameters {
    values = Map.copyOf(values);
  }

  public static CheckAlgorithmParameters parse(final String yaml) {
    final Object document;
    try {
      final LoaderOptions loaderOptions = new LoaderOptions();
      final DumperOptions dumperOptions = new DumperOptions();
      document =
          new Yaml(
                  new SafeConstructor(loaderOptions),
                  new Representer(dumperOptions),
                  dumperOptions,
                  loaderOptions,
                  new ConfigurationYamlResolver())
              .load(yaml);
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
            checkNames(name.toString(), value);
            values.put(name.toString(), value);
          }
        });
    return new CheckAlgorithmParameters(values);
  }

  private static void checkNames(final String path, final Object value) {
    if (value instanceof List<?> list) {
      list.forEach(element -> checkName(path, element));
    } else if (value instanceof Map<?, ?> mapping) {
      mapping.forEach(
          (key, element) -> {
            checkName(path, key);
            if (element != null) {
              checkNames("%s/%s".formatted(path, key), element);
            }
          });
    }
  }

  private static void checkName(final String path, @Nullable final Object name) {
    if (name == null) {
      throw new IllegalArgumentException(GT._T("Empty value in %s", path));
    }
    if (name instanceof Boolean) {
      throw new IllegalArgumentException(
          GT._T("Value \"%s\" in %s is not a name, write it between quotes", name, path));
    }
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
