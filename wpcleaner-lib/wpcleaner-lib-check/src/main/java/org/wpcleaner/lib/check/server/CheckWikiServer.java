package org.wpcleaner.lib.check.server;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.api.UserAgent;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@Service
public class CheckWikiServer {

  public static final String BOTS_URL =
      "https://checkwiki.toolforge.org/cgi-bin/checkwiki_bots.cgi";

  private static final int MAX_ERROR_NUMBER_WITH_LIST = 500;
  private static final String TITLE_PREFIX = "title=";
  private static final Pattern ELEMENT_SEPARATOR = Pattern.compile("\\|");

  private final RestClient.Builder clientBuilder;
  private final UserAgent userAgent;
  private final CheckAlgorithmRegistry algorithmRegistry;

  public CheckWikiServer(
      final RestClient.Builder clientBuilder,
      final UserAgent userAgent,
      final CheckAlgorithmRegistry algorithmRegistry) {
    this.clientBuilder = clientBuilder;
    this.userAgent = userAgent;
    this.algorithmRegistry = algorithmRegistry;
  }

  public static MultiValueMap<String, String> createListParameters(
      final WikiDefinition wiki, final int errorNumber, final int limit) {
    final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("action", "list");
    parameters.add("id", Integer.toString(errorNumber));
    parameters.add("limit", Integer.toString(limit));
    parameters.add("offset", "0");
    parameters.add("project", getProject(wiki));
    return parameters;
  }

  public static MultiValueMap<String, String> createMarkParameters(
      final WikiDefinition wiki, final int errorNumber, final String title) {
    final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("action", "mark");
    parameters.add("id", Integer.toString(errorNumber));
    parameters.add("project", getProject(wiki));
    parameters.add("title", title);
    return parameters;
  }

  public List<String> retrievePages(
      final WikiDefinition wiki, final int errorNumber, final int limit) {
    if (wiki.checkWikiCode() == null) {
      return List.of();
    }
    return Optional.ofNullable(post(createListParameters(wiki, errorNumber, limit))).stream()
        .flatMap(String::lines)
        .flatMap(line -> Arrays.stream(ELEMENT_SEPARATOR.split(line, -1)))
        .filter(element -> element.startsWith(TITLE_PREFIX))
        .map(element -> element.substring(TITLE_PREFIX.length()).trim())
        .filter(title -> !title.isEmpty())
        .toList();
  }

  public List<CheckAlgorithm> markAsFixed(
      final PageAnalysis analysis, final List<CheckAlgorithm> algorithms) {
    final WikiDefinition wiki = analysis.getWiki();
    if (wiki.checkWikiCode() == null
        || analysis.getPageTitle().namespace() != CommonNamespaces.MAIN.id) {
      return List.of();
    }
    final List<CheckAlgorithm> markedAlgorithms =
        algorithms.stream()
            .filter(algorithm -> algorithm.number() <= MAX_ERROR_NUMBER_WITH_LIST)
            .filter(algorithm -> !algorithmRegistry.isBotOnly(wiki, algorithm))
            .toList();
    markedAlgorithms.forEach(
        algorithm -> post(createMarkParameters(wiki, algorithm.number(), analysis.getTitle())));
    return markedAlgorithms;
  }

  @Nullable
  private String post(final MultiValueMap<String, String> parameters) {
    return clientBuilder
        .build()
        .post()
        .uri(BOTS_URL)
        .header(HttpHeaders.USER_AGENT, userAgent.getValue())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(parameters)
        .retrieve()
        .body(String.class);
  }

  private static String getProject(final WikiDefinition wiki) {
    return String.valueOf(wiki.checkWikiCode()).replace('-', '_');
  }
}
