package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiMoveTest {

  private static final MoveQueryCommon COMMON =
      MoveQueryCommon.emptyBuilder().to("User:Test/Draft").token("test-token+\\").build();

  @Autowired private ApiMove apiMove;
  @Autowired private MockApiMove mockApiMove;

  @DisplayName("ApiMove by title sends all options and returns the move")
  @Test
  void moveByTitle() {
    final MoveQueryCommon common =
        MoveQueryCommon.emptyBuilder()
            .to("User:Test/Draft")
            .reason("Drafting page & moving")
            .moveTalk(true)
            .moveSubpages(false)
            .noRedirect(true)
            .watchlist(MoveParameters.Watchlist.WATCH.value)
            .watchlistExpiry(Instant.parse("2026-12-31T00:00:00Z"))
            .ignoreWarnings(true)
            .tags(List.of("tag1", "tag2"))
            .token("test-token+\\")
            .build();
    final MoveQuery query = new MoveQueryByTitle("User:Test", common);
    final Move expectedMove =
        new Move("User:Test", "Drafting page & moving", false, "User:Test/Draft");
    mockApiMove.givenMove(WikipediaDefinitions.EN, query).willReturn(expectedMove);

    final Move move = apiMove.move(WikipediaDefinitions.EN, query);

    Assertions.assertThat(move).isEqualTo(expectedMove);
  }

  @DisplayName("ApiMove by page id sends fromid")
  @Test
  void moveByPageId() {
    final MoveQuery query = new MoveQueryByPageId(12_345, COMMON);
    final Move expectedMove = new Move("User:Test", null, true, "User:Test/Draft");
    mockApiMove.givenMove(WikipediaDefinitions.EN, query).willReturn(expectedMove);

    final Move move = apiMove.move(WikipediaDefinitions.EN, query);

    Assertions.assertThat(move).isEqualTo(expectedMove);
  }

  @DisplayName("ApiMove throws ApiException when MediaWiki returns errors")
  @Test
  void moveWithErrors() {
    final MoveQuery query = new MoveQueryByTitle("User:Test", COMMON);
    mockApiMove
        .givenMove(WikipediaDefinitions.EN, query)
        .willReturnErrors(
            List.of(new ApiError("articleexists", "move", "A page of that name already exists.")));

    Assertions.assertThatThrownBy(() -> apiMove.move(WikipediaDefinitions.EN, query))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception ->
                Assertions.assertThat(exception.getDetails())
                    .isEqualTo("articleexists: A page of that name already exists."));
  }

  @DisplayName("ApiMove fails when MediaWiki server fails")
  @Test
  void moveWhenServerFails() {
    final MoveQuery query = new MoveQueryByTitle("User:Test", COMMON);
    mockApiMove.givenMove(WikipediaDefinitions.EN, query).willFail();

    Assertions.assertThatThrownBy(() -> apiMove.move(WikipediaDefinitions.EN, query))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
