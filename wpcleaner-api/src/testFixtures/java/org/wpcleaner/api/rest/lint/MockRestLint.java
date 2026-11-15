package org.wpcleaner.api.rest.lint;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockRestLint {

  private final MockRestServiceServer mediaWikiServer;

  public MockRestLint(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public ApiAnswer givenLint(final WikiDefinition wiki, final String title, final String text) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofRestPost(
            mediaWikiServer,
            UriComponentsBuilder.fromUriString(wiki.restUrl() + RestLint.LINT_PATH)
                .encode()
                .buildAndExpand(title.replace(' ', '_'))
                .toUri());
    request.bodyParam(RestLint.WIKITEXT, text);
    return request.answer();
  }
}
