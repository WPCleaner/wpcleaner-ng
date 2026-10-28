package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.context.annotation.Bean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.query.prop.langlinks.MockApiLanglinks;

public class MediaWikiServerConfiguration {

  @Bean
  public MockRestServiceServer mediaWikiServer(final RestClient.Builder apiRestClientBuilder) {
    return MockRestServiceServer.bindTo(apiRestClientBuilder).build();
  }

  @Bean
  public MockApiMove mockApiMove(final MockRestServiceServer mediaWikiServer) {
    return new MockApiMove(mediaWikiServer);
  }

  @Bean
  public MockApiLanglinks mockApiLanglinks(final MockRestServiceServer mediaWikiServer) {
    return new MockApiLanglinks(mediaWikiServer);
  }
}
