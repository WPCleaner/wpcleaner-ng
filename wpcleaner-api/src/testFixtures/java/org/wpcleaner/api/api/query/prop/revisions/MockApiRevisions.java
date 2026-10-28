package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
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

public final class MockApiRevisions {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiRevisions(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public RevisionsAnswer givenRetrieveRevisionsByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final RevisionsQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.REVISIONS.value);
    request.queryParamCollection(QueryParameters.TITLES.value, titles);
    if (options != null) {
      expectOptions(request, options);
    }
    return new RevisionsAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final RevisionsQuery options) {
    request.queryParam(RevisionsParameters.CONTENT_FORMAT_MAIN.value, options.contentFormatMain());
    request.queryParam(RevisionsParameters.CONTINUE.value, options.rvContinue());
    request.queryParam(
        RevisionsParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    request.queryParam(RevisionsParameters.END.value, options.end());
    request.queryParam(RevisionsParameters.END_ID.value, options.endId());
    request.queryParam(RevisionsParameters.EXCLUDE_USER.value, options.excludeUser());
    request.queryParam(RevisionsParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        RevisionsParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    request.queryParam(RevisionsParameters.SECTION.value, options.section());
    request.queryParamCollection(RevisionsParameters.SLOTS.value, options.slots());
    request.queryParam(RevisionsParameters.START.value, options.start());
    request.queryParam(RevisionsParameters.START_ID.value, options.startId());
    request.queryParam(RevisionsParameters.TAG.value, options.tag());
    request.queryParam(RevisionsParameters.USER.value, options.user());
  }

  public record RevisionsAnswer(ApiAnswer answer) {

    public void willReturn(final List<Page> pages) {
      answer.willReturnQuery("pages", pages.stream().map(RevisionsAnswer::createPage).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createPage(final Page page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("pageid", page.pageId());
      result.put("ns", page.ns());
      result.put("title", page.title());
      result.put("missing", page.missing() ? Boolean.TRUE : null);
      result.put(
          "revisions",
          page.revisions().isEmpty()
              ? null
              : page.revisions().stream().map(RevisionsAnswer::createRevision).toList());
      return result;
    }

    private static Map<String, @Nullable Object> createRevision(final Revision revision) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("revid", revision.revId());
      result.put("parentid", revision.parentId());
      result.put("minor", revision.minor() ? Boolean.TRUE : null);
      result.put("user", revision.user());
      result.put("userid", revision.userId());
      result.put("timestamp", formatInstant(revision.timestamp()));
      result.put("size", revision.size());
      result.put("sha1", revision.sha1());
      result.put("comment", revision.comment());
      result.put("parsedcomment", revision.parsedComment());
      result.put("tags", revision.tags().isEmpty() ? null : revision.tags());
      result.put("slots", revision.slots().isEmpty() ? null : revision.slots());
      return result;
    }

    @Nullable
    private static String formatInstant(@Nullable final Instant instant) {
      return instant == null ? null : instant.toString();
    }
  }
}
