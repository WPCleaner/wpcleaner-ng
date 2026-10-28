package org.wpcleaner.api.api.query.list.tags;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiTags {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiTags(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public TagsAnswer givenRetrieveTags(
      final WikiDefinition wiki,
      @Nullable final Limit limit,
      @Nullable final String tgContinue,
      @Nullable final Collection<TagsParameters.Properties> properties) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.TAGS.value);
    request.queryParam(TagsParameters.LIMIT.value, limit, Limit::value);
    request.queryParam(TagsParameters.CONTINUE.value, tgContinue);
    request.queryParamCollection(TagsParameters.PROPERTIES.value, properties, prop -> prop.value);
    return new TagsAnswer(request.answer());
  }

  public record TagsAnswer(ApiAnswer answer) {

    public void willReturn(final List<Tag> tags) {
      answer.willReturnQuery("tags", tags.stream().map(TagsAnswer::createTag).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createTag(final Tag tag) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("name", tag.name());
      result.put("displayname", tag.displayName());
      result.put("description", tag.description());
      result.put("hitcount", tag.hitCount());
      result.put("defined", tag.defined() ? Boolean.TRUE : null);
      result.put("active", tag.active() ? Boolean.TRUE : null);
      result.put("source", tag.source().isEmpty() ? null : tag.source());
      return result;
    }
  }
}
