package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters.FilterInterwiki;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiSiteInfoTest {

  private static final Set<SiteInfoParameters.Properties> PROPERTIES =
      Set.of(
          SiteInfoParameters.Properties.INTERWIKI_MAP,
          SiteInfoParameters.Properties.NAMESPACES,
          SiteInfoParameters.Properties.NAMESPACE_ALIASES,
          SiteInfoParameters.Properties.PROTOCOLS);

  @Autowired private ApiSiteInfo apiSiteInfo;
  @Autowired private MockApiSiteInfo mockApiSiteInfo;

  @DisplayName("Request site info returns the site info given to the mock")
  @Test
  void requestSiteInfo() {
    final SiteInfo siteInfo =
        new SiteInfo(
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null,
            List.of(new Interwiki(null, "fr", true, "fr", "https://fr.wikipedia.org/wiki/$1")),
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(new NamespaceAlias("U", 2)),
            Map.of(
                "2", new Namespace("User", "first-letter", false, 2, "User", "User", true),
                "3",
                    new Namespace(
                        "User talk", "first-letter", false, 3, "User talk", "User talk", true)),
            List.of("https://", "mailto:"),
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            null,
            List.of(),
            List.of());
    mockApiSiteInfo
        .givenRequestSiteInfo(
            WikipediaDefinitions.EN, PROPERTIES, FilterInterwiki.LOCAL, Boolean.TRUE, Boolean.FALSE)
        .willReturn(siteInfo);

    final SiteInfo result =
        apiSiteInfo.requestSiteInfo(
            WikipediaDefinitions.EN,
            PROPERTIES,
            FilterInterwiki.LOCAL,
            Boolean.TRUE,
            Boolean.FALSE);

    Assertions.assertThat(result).isEqualTo(siteInfo);
  }

  @DisplayName("Request site info fails when the mock fails")
  @Test
  void requestSiteInfoWhenFailing() {
    mockApiSiteInfo
        .givenRequestSiteInfo(WikipediaDefinitions.EN, List.of(), null, null, null)
        .willFail();

    Assertions.assertThatThrownBy(
            () -> apiSiteInfo.requestSiteInfo(WikipediaDefinitions.EN, List.of(), null, null, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
