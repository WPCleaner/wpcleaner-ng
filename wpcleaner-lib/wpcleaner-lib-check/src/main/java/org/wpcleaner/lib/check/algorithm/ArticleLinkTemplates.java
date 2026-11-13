package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.template.TemplateBuilder;
import org.wpcleaner.lib.check.configuration.TemplateDefinition;

record ArticleLinkTemplates(
    List<TemplateDefinition> historyTemplates,
    List<TemplateDefinition> oldidTemplates,
    List<TemplateDefinition> prefixIndexTemplates) {

  static final String ARTICLE_FIELD = "article";
  static final String OLDID_FIELD = "oldid";
  static final List<String> HISTORY_FIELDS = List.of(ARTICLE_FIELD, ArticleLink.TEXT_FIELD);
  static final List<String> OLDID_FIELDS =
      List.of(ARTICLE_FIELD, OLDID_FIELD, ArticleLink.TEXT_FIELD);
  static final List<String> PREFIX_INDEX_FIELDS = HISTORY_FIELDS;
  private static final String ACTION_ATTRIBUTE = "action";
  private static final String HISTORY_ACTION = "history";
  private static final String OLDID_ATTRIBUTE = "oldid";
  private static final String PREFIX_ATTRIBUTE = "prefix";
  private static final String NAMESPACE_ATTRIBUTE = "namespace";
  private static final Set<String> PREFIX_INDEX_ATTRIBUTES =
      Set.of(PREFIX_ATTRIBUTE, NAMESPACE_ATTRIBUTE);

  ArticleLinkTemplates {
    historyTemplates = List.copyOf(historyTemplates);
    oldidTemplates = List.copyOf(oldidTemplates);
    prefixIndexTemplates = List.copyOf(prefixIndexTemplates);
  }

  List<String> createReplacements(final ArticleLink link, final boolean prefixIndex) {
    final Map<String, String> attributes = link.url().attributes();
    if (prefixIndex) {
      return findPrefix(attributes)
          .map(prefix -> create(prefixIndexTemplates, PREFIX_INDEX_FIELDS, prefix, link.text()))
          .orElse(List.of());
    }
    final String oldid = attributes.get(OLDID_ATTRIBUTE);
    if (attributes.size() == 1 && oldid != null && !oldid.isEmpty()) {
      return create(oldidTemplates, OLDID_FIELDS, link.url().title(), oldid, link.text());
    }
    if (attributes.size() == 1 && HISTORY_ACTION.equals(attributes.get(ACTION_ATTRIBUTE))) {
      return create(historyTemplates, HISTORY_FIELDS, link.url().title(), link.text());
    }
    return List.of();
  }

  private static Optional<String> findPrefix(final Map<String, String> attributes) {
    if (!PREFIX_INDEX_ATTRIBUTES.containsAll(attributes.keySet())) {
      return Optional.empty();
    }
    return Optional.ofNullable(attributes.get(PREFIX_ATTRIBUTE))
        .map(String::stripTrailing)
        .filter(prefix -> !prefix.isEmpty());
  }

  private static List<String> create(
      final List<TemplateDefinition> templates,
      final List<String> fields,
      final @Nullable String... values) {
    return templates.stream()
        .map(
            template -> {
              final List<TemplateBuilder.Parameter> parameters = new ArrayList<>();
              for (int index = 0; index < fields.size(); index++) {
                final String value = values[index];
                if (value != null) {
                  parameters.add(
                      new TemplateBuilder.Parameter(
                          template.parameter(fields.get(index)).orElse(Integer.toString(index + 1)),
                          value));
                }
              }
              return new TemplateBuilder(template.template(), parameters).build();
            })
        .collect(Collectors.toList());
  }
}
