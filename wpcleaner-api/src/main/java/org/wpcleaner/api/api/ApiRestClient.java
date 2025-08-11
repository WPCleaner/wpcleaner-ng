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
public class ApiRestClient {

  private final ApiLogInterceptor logInterceptor;
  private final RestClient.Builder clientBuilder;
  private final Map<WikiDefinition, RestClient> restClients;

  public ApiRestClient(final ApiLogInterceptor logInterceptor) {
    this.logInterceptor = logInterceptor;
    this.clientBuilder = RestClient.builder();
    this.restClients = new ConcurrentHashMap<>();
  }

  public RestClient getRestClient(final WikiDefinition wiki) {
    return restClients.computeIfAbsent(wiki, this::createRestClient);
  }

  private RestClient createRestClient(final WikiDefinition wiki) {
    return clientBuilder
        .baseUrl(wiki.apiUrl())
        .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8")
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .defaultHeader(HttpHeaders.USER_AGENT, "WPCleaner")
        // TODO: https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy
        .requestInterceptor(logInterceptor)
        .build();
  }
}
