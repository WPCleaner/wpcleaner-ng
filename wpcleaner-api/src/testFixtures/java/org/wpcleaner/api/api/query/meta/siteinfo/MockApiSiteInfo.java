package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoParameters.FilterInterwiki;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiSiteInfo {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiSiteInfo(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public SiteInfoAnswer givenRequestSiteInfo(
      final WikiDefinition wiki,
      final Collection<SiteInfoParameters.Properties> properties,
      @Nullable final FilterInterwiki filterInterwiki,
      @Nullable final Boolean showImageList,
      @Nullable final Boolean numberInGroup) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.META.value, QueryParameters.Meta.SITE_INFO.value);
    request.queryParamCollection(
        SiteInfoParameters.PROPERTIES.value, properties, property -> property.value);
    request.queryParam(
        SiteInfoParameters.FILTER_INTERWIKI.value, filterInterwiki, filter -> filter.value);
    request.queryParam(SiteInfoParameters.SHOW_IMAGE_LIST.value, showImageList);
    request.queryParam(SiteInfoParameters.NUMBER_IN_GROUP.value, numberInGroup);
    return new SiteInfoAnswer(request.answer());
  }

  public record SiteInfoAnswer(ApiAnswer answer) {

    public void willReturn(final SiteInfo siteInfo) {
      answer.willReturnQuery(siteInfo);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
