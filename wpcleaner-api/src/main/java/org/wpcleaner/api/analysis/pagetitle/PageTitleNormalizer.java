package org.wpcleaner.api.analysis.pagetitle;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.web.util.HtmlUtils;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;

public final class PageTitleNormalizer {

  private static final Pattern SPACES =
      Pattern.compile(
          "[ _\\u00A0\\u1680\\u180E\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000]+");
  private static final String SPACE = " ";
  private static final char FRAGMENT = '#';
  private static final char CHARACTER_REFERENCE = '&';
  private static final char NAMESPACE_SEPARATOR = ':';

  private final List<Namespace> namespaces;

  public PageTitleNormalizer(final List<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  public Optional<PageTitle> normalize(final String name, final int defaultNamespace) {
    final String decodedName =
        name.indexOf(CHARACTER_REFERENCE) < 0 ? name : HtmlUtils.htmlUnescape(name);
    final int fragment = decodedName.indexOf(FRAGMENT);
    String text = normalizeSpaces(fragment < 0 ? decodedName : decodedName.substring(0, fragment));
    int namespace = defaultNamespace;
    if (!text.isEmpty() && text.charAt(0) == NAMESPACE_SEPARATOR) {
      namespace = CommonNamespaces.MAIN.id;
      text = normalizeSpaces(text.substring(1));
    }
    final int separator = text.indexOf(NAMESPACE_SEPARATOR);
    if (separator > 0) {
      final Optional<Namespace> prefixNamespace =
          findNamespace(normalizeSpaces(text.substring(0, separator)));
      if (prefixNamespace.isPresent()) {
        namespace = prefixNamespace.get().id();
        text = normalizeSpaces(text.substring(separator + 1));
      }
    }
    if (text.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new PageTitle(namespace, findCaseType(namespace).normalize(text)));
  }

  private Optional<Namespace> findNamespace(final String prefix) {
    final String lowerCasePrefix = prefix.toLowerCase(Locale.ROOT);
    return namespaces.stream()
        .filter(namespace -> namespace.id() != CommonNamespaces.MAIN.id)
        .filter(
            namespace ->
                Stream.concat(
                        Stream.of(namespace.canonical(), namespace.name()),
                        namespace.aliases().stream())
                    .map(PageTitleNormalizer::normalizeSpaces)
                    .anyMatch(value -> value.toLowerCase(Locale.ROOT).equals(lowerCasePrefix)))
        .findFirst();
  }

  private CaseType findCaseType(final int namespace) {
    return Namespace.findNamespace(namespaces, namespace)
        .map(Namespace::caseType)
        .orElse(CaseType.FIRST_LETTER);
  }

  private static String normalizeSpaces(final String value) {
    return SPACES.matcher(value).replaceAll(SPACE).strip();
  }
}
