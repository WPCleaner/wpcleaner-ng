/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.api.api.edit;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.move.ApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQuery;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.repository.token.TokenRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

class ApiEditWrapperTest {

  @DisplayName("ApiEditWrapper executes edits on a daemon thread named ApiEditWrapper")
  @Test
  void testEditRunsOnDaemonThread() {
    final ApiEdit apiEdit = BDDMockito.mock(ApiEdit.class);
    final ApiMove apiMove = BDDMockito.mock(ApiMove.class);
    final TagRepository tagRepository = BDDMockito.mock(TagRepository.class);
    final TokenRepository tokenRepository = BDDMockito.mock(TokenRepository.class);
    final CurrentUserService user = BDDMockito.mock(CurrentUserService.class);

    final ConnectedUser connectedUser = BDDMockito.mock(ConnectedUser.class);
    BDDMockito.given(connectedUser.demo()).willReturn(false);
    BDDMockito.given(connectedUser.isLoggedIn()).willReturn(true);
    BDDMockito.given(user.getCurrentUser()).willReturn(connectedUser);

    final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
    final EditQueryCommon common = EditQueryCommon.emptyBuilder().summary("test summary").build();
    final EditQuery query = new EditQueryByTitle("Test", common);

    final AtomicBoolean isDaemon = new AtomicBoolean(false);
    final AtomicReference<String> threadName = new AtomicReference<>();

    BDDMockito.given(apiEdit.edit(BDDMockito.eq(wiki), BDDMockito.any(EditQuery.class)))
        .willAnswer(
            _ -> {
              isDaemon.set(Thread.currentThread().isDaemon());
              threadName.set(Thread.currentThread().getName());
              return BDDMockito.mock(Edit.class);
            });

    final ApiEditWrapper wrapper =
        new ApiEditWrapper(apiEdit, apiMove, tagRepository, tokenRepository, user);
    wrapper.edit(wiki, query);

    Assertions.assertThat(isDaemon.get()).isTrue();
    Assertions.assertThat(threadName.get()).isEqualTo("ApiEditWrapper");
    wrapper.destroy();
  }

  @DisplayName("ApiEditWrapper executes moves on a daemon thread named ApiEditWrapper")
  @Test
  void testMoveRunsOnDaemonThread() {
    final ApiEdit apiEdit = BDDMockito.mock(ApiEdit.class);
    final ApiMove apiMove = BDDMockito.mock(ApiMove.class);
    final TagRepository tagRepository = BDDMockito.mock(TagRepository.class);
    final TokenRepository tokenRepository = BDDMockito.mock(TokenRepository.class);
    final CurrentUserService user = BDDMockito.mock(CurrentUserService.class);

    final ConnectedUser connectedUser = BDDMockito.mock(ConnectedUser.class);
    BDDMockito.given(connectedUser.demo()).willReturn(false);
    BDDMockito.given(connectedUser.isLoggedIn()).willReturn(true);
    BDDMockito.given(user.getCurrentUser()).willReturn(connectedUser);

    final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
    final MoveQueryCommon common =
        MoveQueryCommon.emptyBuilder().to("User:Test/Draft").reason("Drafting").build();
    final MoveQuery query = new MoveQueryByTitle("User:Test", common);

    final AtomicBoolean isDaemon = new AtomicBoolean(false);
    final AtomicReference<String> threadName = new AtomicReference<>();

    BDDMockito.given(apiMove.move(BDDMockito.eq(wiki), BDDMockito.any(MoveQuery.class)))
        .willAnswer(
            _ -> {
              isDaemon.set(Thread.currentThread().isDaemon());
              threadName.set(Thread.currentThread().getName());
              return BDDMockito.mock(Move.class);
            });

    final ApiEditWrapper wrapper =
        new ApiEditWrapper(apiEdit, apiMove, tagRepository, tokenRepository, user);
    wrapper.move(wiki, query);

    Assertions.assertThat(isDaemon.get()).isTrue();
    Assertions.assertThat(threadName.get()).isEqualTo("ApiEditWrapper");
    wrapper.destroy();
  }

  @DisplayName("ApiEditWrapper destroys executor cleanly")
  @Test
  void testDestroyShutsDownExecutor() {
    final ApiEdit apiEdit = BDDMockito.mock(ApiEdit.class);
    final ApiMove apiMove = BDDMockito.mock(ApiMove.class);
    final TagRepository tagRepository = BDDMockito.mock(TagRepository.class);
    final TokenRepository tokenRepository = BDDMockito.mock(TokenRepository.class);
    final CurrentUserService user = BDDMockito.mock(CurrentUserService.class);

    final ApiEditWrapper wrapper =
        new ApiEditWrapper(apiEdit, apiMove, tagRepository, tokenRepository, user);
    wrapper.destroy();

    final WikiDefinition wiki = BDDMockito.mock(WikiDefinition.class);
    final EditQueryCommon common = EditQueryCommon.emptyBuilder().summary("test summary").build();
    final EditQuery query = new EditQueryByTitle("Test", common);

    Assertions.assertThatThrownBy(() -> wrapper.edit(wiki, query));
  }
}
