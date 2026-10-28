package org.wpcleaner.api.api.move;

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

public final class MockApiMove {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiMove(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public MoveAnswer givenMove(final WikiDefinition wiki, final MoveQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofPost(mediaWikiServer, wiki, ApiParameters.Action.MOVE);
    switch (query) {
      case MoveQueryByTitle byTitle -> request.bodyParam(MoveParameters.FROM.value, byTitle.from());
      case MoveQueryByPageId byPageId ->
          request.bodyParam(MoveParameters.FROM_ID.value, byPageId.fromId());
    }
    final MoveQueryCommon common = query.common();
    request.bodyParam(MoveParameters.TO.value, common.to());
    request.bodyParam(MoveParameters.REASON.value, common.reason());
    request.bodyParam(MoveParameters.MOVE_TALK.value, common.moveTalk());
    request.bodyParam(MoveParameters.MOVE_SUBPAGES.value, common.moveSubpages());
    request.bodyParam(MoveParameters.NO_REDIRECT.value, common.noRedirect());
    request.bodyParam(MoveParameters.WATCHLIST.value, common.watchlist());
    request.bodyParam(MoveParameters.WATCHLIST_EXPIRY.value, common.watchlistExpiry());
    request.bodyParam(MoveParameters.IGNORE_WARNINGS.value, common.ignoreWarnings());
    request.bodyParam(MoveParameters.TAGS.value, common.tags());
    request.bodyParam(MoveParameters.TOKEN.value, common.token());
    return new MoveAnswer(request.answer());
  }

  public record MoveAnswer(ApiAnswer answer) {

    public void willReturn(final Move move) {
      answer.willReturn(Map.of("move", move));
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
