package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ApiLogInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  // TODO: Handle sensitive parameters properly (a repository, each api adds its own set)
  private static final Set<String> SENSITIVE_QUERY_PARAMS = Set.of();

  private final boolean logBody;

  public ApiLogInterceptor(
      @Value("${wpcleaner.api.log-interceptor.body:false}") final boolean logBody) {
    this.logBody = logBody;
  }

  @Override
  @Nonnull
  public ClientHttpResponse intercept(
      @Nonnull final HttpRequest request,
      @Nonnull final byte[] body,
      @Nonnull final ClientHttpRequestExecution execution)
      throws IOException {
    LOGGER.info("Start outbound request {} {}", request.getMethod(), cleanUri(request.getURI()));
    if (logBody && body.length > 0) {
      LOGGER.info("  Request body: {}", new String(body, StandardCharsets.UTF_8));
    }
    return execution.execute(request, body);
  }

  private URI cleanUri(final URI uri) {
    final MultiValueMap<String, String> queryParams =
        UriComponentsBuilder.fromUri(uri).build().getQueryParams();
    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
    for (final String sensitiveQueryParam : SENSITIVE_QUERY_PARAMS) {
      if (queryParams.containsKey(sensitiveQueryParam)) {
        builder = builder.replaceQueryParam(sensitiveQueryParam, "****");
      }
    }
    return builder.build().toUri();
  }
}
