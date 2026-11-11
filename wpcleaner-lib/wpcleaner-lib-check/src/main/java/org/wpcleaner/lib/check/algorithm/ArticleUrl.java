package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record ArticleUrl(String title, Map<String, String> attributes, @Nullable String fragment) {

  private static final List<String> PROTOCOLS = List.of("http://", "https://", "//");
  private static final String TITLE_ATTRIBUTE = "title";
  private static final char SLASH = '/';

  ArticleUrl {
    attributes = Map.copyOf(attributes);
  }

  static Optional<ArticleUrl> of(final WikiDefinition wiki, final String url) {
    return cleanUrl(url).flatMap(ArticleUrl::toUri).flatMap(uri -> of(wiki, uri));
  }

  private static Optional<String> cleanUrl(final String url) {
    final String lowerCaseUrl = url.toLowerCase(Locale.ROOT);
    return PROTOCOLS.stream()
        .filter(lowerCaseUrl::startsWith)
        .findFirst()
        .map(
            protocol -> {
              int hostBegin = protocol.length();
              while (hostBegin < url.length() && url.charAt(hostBegin) == SLASH) {
                hostBegin++;
              }
              return url.substring(0, protocol.length()) + url.substring(hostBegin);
            });
  }

  private static Optional<URI> toUri(final String url) {
    try {
      return Optional.of(new URI(url));
    } catch (final URISyntaxException _) {
      return Optional.empty();
    }
  }

  private static Optional<ArticleUrl> of(final WikiDefinition wiki, final URI uri) {
    final String host = uri.getHost();
    if (host == null
        || wiki.hosts().stream().noneMatch(wikiHost -> wikiHost.equalsIgnoreCase(host))) {
      return Optional.empty();
    }
    final Map<String, String> attributes = new LinkedHashMap<>();
    if (uri.getRawQuery() != null) {
      parseQuery(uri.getRawQuery(), attributes);
    }
    final String path = uri.getPath() == null ? "" : uri.getPath();
    final String articlePrefix = wiki.wikiPath() + SLASH;
    final String title;
    if (path.startsWith(articlePrefix)) {
      title = path.substring(articlePrefix.length());
    } else if (path.equals(wiki.indexPath())) {
      title = attributes.remove(TITLE_ATTRIBUTE);
    } else {
      return Optional.empty();
    }
    return Optional.ofNullable(title)
        .map(ArticleUrl::normalizeTitle)
        .filter(normalizedTitle -> !normalizedTitle.isEmpty())
        .map(normalizedTitle -> new ArticleUrl(normalizedTitle, attributes, uri.getFragment()));
  }

  private static void parseQuery(final String query, final Map<String, String> attributes) {
    for (final String parameter : query.split("&", -1)) {
      final String[] parts = parameter.split("=", 2);
      attributes.put(decode(parts[0]), parts.length > 1 ? decode(parts[1]) : "");
    }
  }

  private static String decode(final String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private static String normalizeTitle(final String title) {
    String normalizedTitle = title.replace('_', ' ').replaceAll(" {2,}", " ").trim();
    while (!normalizedTitle.isEmpty()
        && normalizedTitle.charAt(normalizedTitle.length() - 1) == SLASH) {
      normalizedTitle = normalizedTitle.substring(0, normalizedTitle.length() - 1);
    }
    return normalizedTitle;
  }

  String titleAndFragment() {
    return fragment == null ? title : title + "#" + fragment;
  }
}
