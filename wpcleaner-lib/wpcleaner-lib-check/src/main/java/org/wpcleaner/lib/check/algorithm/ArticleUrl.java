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
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record ArticleUrl(String title, Map<String, String> attributes, @Nullable String fragment) {

  private static final List<String> PROTOCOLS = List.of("http://", "https://", "//");
  private static final String TITLE_ATTRIBUTE = "title";
  private static final char SLASH = '/';
  private static final String TITLE_PARAMETER = "$1";
  private static final char ATTRIBUTE_VALUE = '=';
  private static final char HOST_SEPARATOR = '.';
  private static final String MOBILE_HOST = ".m";

  ArticleUrl {
    attributes = Map.copyOf(attributes);
  }

  static Optional<ArticleUrl> of(final WikiDefinition wiki, final String url) {
    return cleanUrl(url).flatMap(ArticleUrl::toUri).flatMap(uri -> of(wiki, uri));
  }

  static Optional<ArticleUrl> of(final Interwiki interwiki, final String url) {
    final int parameter = interwiki.url().indexOf(TITLE_PARAMETER);
    if (parameter < 0) {
      return Optional.empty();
    }
    final String suffix = interwiki.url().substring(parameter + TITLE_PARAMETER.length());
    return cleanUrl(interwiki.url().substring(0, parameter))
        .flatMap(ArticleUrl::toUri)
        .flatMap(
            base -> cleanUrl(url).flatMap(ArticleUrl::toUri).flatMap(uri -> of(base, suffix, uri)));
  }

  static Optional<String> findHost(final Interwiki interwiki) {
    return cleanUrl(interwiki.url()).flatMap(ArticleUrl::toUri).map(URI::getHost);
  }

  private static Optional<ArticleUrl> of(final URI base, final String suffix, final URI uri) {
    final String host = uri.getHost();
    if (host == null || !isSameHost(host, base.getHost())) {
      return Optional.empty();
    }
    final Map<String, String> attributes = parseQuery(uri);
    final String path = Objects.requireNonNullElse(uri.getPath(), "");
    final String basePath = Objects.requireNonNullElse(base.getPath(), "");
    final Optional<String> title =
        base.getRawQuery() == null
            ? findTitleInPath(path, basePath, suffix)
            : findTitleInQuery(path, basePath, base.getRawQuery() + suffix, attributes);
    return create(title.orElse(null), attributes, uri);
  }

  private static boolean isSameHost(final String host, @Nullable final String baseHost) {
    if (baseHost == null) {
      return false;
    }
    final int firstDot = baseHost.indexOf(HOST_SEPARATOR);
    return host.equalsIgnoreCase(baseHost)
        || (firstDot > 0
            && baseHost.indexOf(HOST_SEPARATOR, firstDot + 1) > 0
            && host.equalsIgnoreCase(
                baseHost.substring(0, firstDot) + MOBILE_HOST + baseHost.substring(firstDot)));
  }

  private static Optional<String> findTitleInPath(
      final String path, final String basePath, final String suffix) {
    if (path.length() < basePath.length() + suffix.length()
        || !path.startsWith(basePath)
        || !path.endsWith(suffix)) {
      return Optional.empty();
    }
    return Optional.of(path.substring(basePath.length(), path.length() - suffix.length()));
  }

  private static Optional<String> findTitleInQuery(
      final String path,
      final String basePath,
      final String baseQuery,
      final Map<String, String> attributes) {
    if (!path.equals(basePath) || baseQuery.indexOf(ATTRIBUTE_VALUE) != baseQuery.length() - 1) {
      return Optional.empty();
    }
    return Optional.ofNullable(attributes.remove(baseQuery.substring(0, baseQuery.length() - 1)));
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
    final Map<String, String> attributes = parseQuery(uri);
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
    return create(title, attributes, uri);
  }

  private static Optional<ArticleUrl> create(
      @Nullable final String title, final Map<String, String> attributes, final URI uri) {
    return Optional.ofNullable(title)
        .map(ArticleUrl::normalizeTitle)
        .filter(normalizedTitle -> !normalizedTitle.isEmpty())
        .map(normalizedTitle -> new ArticleUrl(normalizedTitle, attributes, uri.getFragment()));
  }

  private static Map<String, String> parseQuery(final URI uri) {
    final Map<String, String> attributes = new LinkedHashMap<>();
    if (uri.getRawQuery() != null) {
      for (final String parameter : uri.getRawQuery().split("&", -1)) {
        final String[] parts = parameter.split("=", 2);
        attributes.put(decode(parts[0]), parts.length > 1 ? decode(parts[1]) : "");
      }
    }
    return attributes;
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
