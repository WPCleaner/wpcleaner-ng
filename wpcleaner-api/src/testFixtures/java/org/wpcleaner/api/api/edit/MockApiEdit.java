package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiEdit {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiEdit(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public EditAnswer givenEdit(final WikiDefinition wiki, final EditQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofPost(mediaWikiServer, wiki, ApiParameters.Action.EDIT);
    switch (query) {
      case EditQueryByTitle byTitle -> request.bodyParam("title", byTitle.title());
      case EditQueryByPageId byPageId -> request.bodyParam("pageid", byPageId.pageId());
    }
    final EditQueryCommon common = query.common();
    request.bodyParam("appendtext", common.appendText());
    request.bodyParam("basetimestamp", common.baseTimestamp());
    request.bodyParam("bot", common.bot());
    request.bodyParam("contentformat", common.contentFormat());
    request.bodyParam("contentmodel", common.contentModel());
    request.bodyParam("createonly", common.createOnly());
    request.bodyParam("md5", common.md5());
    request.bodyParam("minor", common.minor());
    request.bodyParam("prependtext", common.prependText());
    request.bodyParam("nocreate", common.noCreate());
    request.bodyParam("notminor", common.notMinor());
    request.bodyParam("recreate", common.recreate());
    request.bodyParam("redirect", common.redirect());
    request.bodyParam("section", common.section());
    request.bodyParam("sectiontitle", common.sectionTitle());
    request.bodyParam("starttimestamp", common.startTimestamp());
    request.bodyParam("summary", common.summary());
    request.bodyParam("tags", common.tags());
    request.bodyParam("text", common.text());
    request.bodyParam("undo", common.undo());
    request.bodyParam("undoafter", common.undoAfter());
    request.bodyParam("watchlist", common.watchlist());
    request.bodyParam("token", common.token());
    return new EditAnswer(request.answer());
  }

  public record EditAnswer(ApiAnswer answer) {

    public void willReturn(final Edit edit) {
      answer.willReturn(Map.of("edit", edit));
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
