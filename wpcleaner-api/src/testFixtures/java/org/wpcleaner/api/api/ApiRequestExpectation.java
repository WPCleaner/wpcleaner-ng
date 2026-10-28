package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class ApiRequestExpectation {

  private final ResponseActions request;
  private final MultiValueMap<String, String> body;

  public static ApiRequestExpectation of(
      final MockRestServiceServer mediaWikiServer,
      final WikiDefinition wiki,
      final ApiParameters.Action action) {
    return expect(mediaWikiServer, wiki, action, HttpMethod.GET);
  }

  public static ApiRequestExpectation ofPost(
      final MockRestServiceServer mediaWikiServer,
      final WikiDefinition wiki,
      final ApiParameters.Action action) {
    final ApiRequestExpectation expectation =
        expect(mediaWikiServer, wiki, action, HttpMethod.POST);
    expectation.request.andExpect(
        MockRestRequestMatchers.content()
            .contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED));
    return expectation;
  }

  private static ApiRequestExpectation expect(
      final MockRestServiceServer mediaWikiServer,
      final WikiDefinition wiki,
      final ApiParameters.Action action,
      final HttpMethod method) {
    final ApiRequestExpectation expectation =
        new ApiRequestExpectation(
            mediaWikiServer
                .expect(MockRestRequestMatchers.requestTo(Matchers.startsWith(wiki.apiUrl())))
                .andExpect(MockRestRequestMatchers.method(method)));
    expectation.queryParam(ApiParameters.ACTION.value, action.value);
    expectation.queryParam(
        ApiParameters.ERROR_FORMAT.value, ApiParameters.ErrorFormat.PLAIN_TEXT.value);
    expectation.queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value);
    expectation.queryParam(ApiParameters.FORMAT_VERSION.value, ApiParameters.FORMAT_VERSION_VALUE);
    return expectation;
  }

  private ApiRequestExpectation(final ResponseActions request) {
    this.request = request;
    this.body = new LinkedMultiValueMap<>();
  }

  public ApiAnswer answer() {
    request.andExpect(formBody(body));
    return new ApiAnswer(request);
  }

  public void bodyParam(final String name, @Nullable final Object value) {
    switch (value) {
      case null -> {}
      case Boolean _ -> {
        if (Boolean.TRUE.equals(value)) {
          body.add(name, "1");
        }
      }
      case List<?> list -> {
        if (!list.isEmpty()) {
          body.add(name, list.stream().map(Object::toString).collect(Collectors.joining("|")));
        }
      }
      default -> body.add(name, value.toString());
    }
  }

  public void queryParam(final String name, final boolean value) {
    if (value) {
      request.andExpect(decodedQueryParam(name, ""));
    }
  }

  public void queryParam(final String name, @Nullable final Object value) {
    queryParam(name, value, Object::toString);
  }

  public <T> void queryParam(
      final String name, @Nullable final T value, final Function<T, @Nullable String> toString) {
    if (value == null) {
      return;
    }
    final String formatted = toString.apply(value);
    if (formatted == null) {
      return;
    }
    request.andExpect(decodedQueryParam(name, formatted));
  }

  public <T> void queryParamCollection(final String name, @Nullable final Collection<T> values) {
    queryParamCollection(name, values, Object::toString);
  }

  public <T> void queryParamCollection(
      final String name, @Nullable final Collection<T> values, final Function<T, String> toString) {
    if (values == null || values.isEmpty()) {
      return;
    }
    final String param = values.stream().map(toString).sorted().collect(Collectors.joining("|"));
    request.andExpect(decodedQueryParam(name, param));
  }

  private static RequestMatcher formBody(final MultiValueMap<String, String> expectedBody) {
    return request -> {
      final String content = ((MockClientHttpRequest) request).getBodyAsString();
      final List<String> parameters =
          content.isEmpty()
              ? List.of()
              : Arrays.stream(content.split("&"))
                  .map(parameter -> URLDecoder.decode(parameter, StandardCharsets.UTF_8))
                  .toList();
      final List<String> expectedParameters =
          expectedBody.entrySet().stream()
              .flatMap(
                  entry -> entry.getValue().stream().map(value -> entry.getKey() + "=" + value))
              .toList();
      AssertionErrors.assertEquals("Form body", expectedParameters, parameters);
    };
  }

  private static RequestMatcher decodedQueryParam(final String name, final String value) {
    return request -> {
      final List<String> values =
          UriComponentsBuilder.fromUri(request.getURI())
              .build()
              .getQueryParams()
              .getOrDefault(name, List.of())
              .stream()
              .map(encoded -> UriUtils.decode(encoded, StandardCharsets.UTF_8))
              .toList();
      AssertionErrors.assertEquals("Query param [" + name + "]", List.of(value), values);
    };
  }
}
