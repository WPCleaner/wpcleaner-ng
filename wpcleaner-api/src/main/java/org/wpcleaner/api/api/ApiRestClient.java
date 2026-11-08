package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiRestClient implements LogoutHook {

  private final ApiLogInterceptor logInterceptor;
  private final RestClient.Builder clientBuilder;
  private final UserAgent userAgent;
  private final Map<WikiDefinition, RestClient> restClients;

  public ApiRestClient(
      final ApiLogInterceptor logInterceptor,
      final RestClient.Builder clientBuilder,
      final UserAgent userAgent) {
    this.logInterceptor = logInterceptor;
    this.clientBuilder = clientBuilder;
    this.userAgent = userAgent;
    this.restClients = new ConcurrentHashMap<>();
  }

  public RestClient getRestClient(final WikiDefinition wiki) {
    return restClients.computeIfAbsent(wiki, this::createRestClient);
  }

  @Override
  public void logout(final ConnectedUser user) {
    restClients.clear();
  }

  private RestClient createRestClient(final WikiDefinition wiki) {
    return clientBuilder
        .clone()
        .baseUrl(wiki.apiUrl())
        .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .defaultRequest(request -> request.header(HttpHeaders.USER_AGENT, userAgent.getValue()))
        .requestInterceptor(logInterceptor)
        .build();
  }
}
