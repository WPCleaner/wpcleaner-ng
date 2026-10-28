package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.context.annotation.Bean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.api.edit.MockApiEdit;
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.langlinks.MockApiLanglinks;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;

public class MediaWikiServerConfiguration {

  @Bean
  public MockRestServiceServer mediaWikiServer(final RestClient.Builder apiRestClientBuilder) {
    return MockRestServiceServer.bindTo(apiRestClientBuilder).build();
  }

  @Bean
  public MockApiEdit mockApiEdit(final MockRestServiceServer mediaWikiServer) {
    return new MockApiEdit(mediaWikiServer);
  }

  @Bean
  public MockApiMove mockApiMove(final MockRestServiceServer mediaWikiServer) {
    return new MockApiMove(mediaWikiServer);
  }

  @Bean
  public MockApiLanglinks mockApiLanglinks(final MockRestServiceServer mediaWikiServer) {
    return new MockApiLanglinks(mediaWikiServer);
  }

  @Bean
  public MockApiRevisions mockApiRevisions(final MockRestServiceServer mediaWikiServer) {
    return new MockApiRevisions(mediaWikiServer);
  }

  @Bean
  public MockApiTokens mockApiTokens(final MockRestServiceServer mediaWikiServer) {
    return new MockApiTokens(mediaWikiServer);
  }
}
