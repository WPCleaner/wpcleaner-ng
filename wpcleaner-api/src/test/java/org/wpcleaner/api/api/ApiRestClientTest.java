package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiRestClientTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private ApiRestClient apiRestClient;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName("should send the user page of a user logged in after the creation of the client")
  @Test
  void userAgentAfterLogin() {
    // GIVEN
    final RestClient restClient = apiRestClient.getRestClient(WIKI);
    currentUserService.login(WIKI, "Some user", false);
    mediaWikiServer
        .expect(
            MockRestRequestMatchers.header(
                HttpHeaders.USER_AGENT,
                Matchers.containsString("; https://fr.wikipedia.org/wiki/User:Some_user)")))
        .andRespond(MockRestResponseCreators.withSuccess());

    // WHEN
    restClient.get().retrieve().toBodilessEntity();

    // THEN
    mediaWikiServer.verify();
  }
}
