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
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
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
    server
        .expect(MockRestRequestMatchers.requestTo(CheckWikiServer.LIST_URL))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
        .andExpect(
            MockRestRequestMatchers.content()
                .formData(CheckWikiServer.createListParameters(wiki, errorNumber, limit)))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                titles.stream().map("title=%s"::formatted).collect(Collectors.joining("\n")),
                new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)));
  }
}
