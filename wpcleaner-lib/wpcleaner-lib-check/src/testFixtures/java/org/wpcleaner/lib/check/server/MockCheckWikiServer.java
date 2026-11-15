package org.wpcleaner.lib.check.server;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.MultiValueMap;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockCheckWikiServer {

  private final MockRestServiceServer server;

  public MockCheckWikiServer(final MockRestServiceServer server) {
    this.server = server;
  }

  public void givenPages(
      final WikiDefinition wiki,
      final int errorNumber,
      final int limit,
      final List<String> titles) {
    expect(CheckWikiServer.BOTS_URL, CheckWikiServer.createListParameters(wiki, errorNumber, limit))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                titles.stream().map("title=%s"::formatted).collect(Collectors.joining("\n")),
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
  }

  public void givenMarkAsFixed(
      final WikiDefinition wiki, final int errorNumber, final String title) {
    expect(CheckWikiServer.BOTS_URL, CheckWikiServer.createMarkParameters(wiki, errorNumber, title))
        .andRespond(MockRestResponseCreators.withSuccess());
  }

  public void givenDetections(
      final WikiDefinition wiki, final String title, final List<String> lines) {
    expect(CheckWikiServer.ARTICLE_URL, CheckWikiServer.createArticleParameters(wiki, title))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                String.join("\n", lines),
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
  }

  private ResponseActions expect(final String url, final MultiValueMap<String, String> parameters) {
    return server
        .expect(MockRestRequestMatchers.requestTo(url))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
        .andExpect(ApiRequestExpectation.userAgent())
        .andExpect(MockRestRequestMatchers.content().formData(parameters));
  }
}
