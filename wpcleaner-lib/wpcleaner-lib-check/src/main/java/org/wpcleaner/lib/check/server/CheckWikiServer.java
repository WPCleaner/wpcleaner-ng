package org.wpcleaner.lib.check.server;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.api.UserAgent;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckWikiServer {

  public static final String LIST_URL =
      "https://checkwiki.toolforge.org/cgi-bin/checkwiki_bots.cgi";

  private static final String TITLE_PREFIX = "title=";
  private static final Pattern ELEMENT_SEPARATOR = Pattern.compile("\\|");

  private final RestClient.Builder clientBuilder;
  private final UserAgent userAgent;

  public CheckWikiServer(final RestClient.Builder clientBuilder, final UserAgent userAgent) {
    this.clientBuilder = clientBuilder;
    this.userAgent = userAgent;
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

  public List<String> retrievePages(
      final WikiDefinition wiki, final int errorNumber, final int limit) {
    if (wiki.checkWikiCode() == null) {
      return List.of();
    }
    final String response =
        clientBuilder
            .clone()
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent.getValue())
            .build()
            .post()
            .uri(LIST_URL)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(createListParameters(wiki, errorNumber, limit))
            .retrieve()
            .body(String.class);
    return Optional.ofNullable(response).stream()
        .flatMap(String::lines)
        .flatMap(line -> Arrays.stream(ELEMENT_SEPARATOR.split(line, -1)))
        .filter(element -> element.startsWith(TITLE_PREFIX))
        .map(element -> element.substring(TITLE_PREFIX.length()).trim())
        .filter(title -> !title.isEmpty())
        .toList();
  }

  private static String getProject(final WikiDefinition wiki) {
    return String.valueOf(wiki.checkWikiCode()).replace('-', '_');
  }
}
