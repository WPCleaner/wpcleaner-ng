package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.move.ApiMove;
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQuery;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.repository.token.TokenRepository;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiEditWrapperTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final WikiDefinition OTHER_WIKI = WikipediaDefinitions.FR;
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final String OTHER_CSRF_TOKEN = "other-csrf-token+\\";
  private static final Duration THROTTLE_PERIOD = Duration.ofMinutes(1);
  private static final Duration SHORT_THROTTLE_PERIOD = Duration.ofSeconds(1);
  private static final Edit EDIT = new Edit(Edit.RESULT_SUCCESS, 1234, "Test", null);
  private static final Move MOVE = new Move("User:Test", "Drafting", false, "User:Test/Draft");

  @Autowired private ApiEdit apiEdit;
  @Autowired private ApiMove apiMove;
  @Autowired private ApiTokens apiTokens;
  @Autowired private KnownDefinitions knownDefinitions;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiMove mockApiMove;
  @Autowired private MockApiTokens mockApiTokens;

  private TagRepository tagRepository;
  private TokenRepository tokenRepository;
  private CurrentUserService user;
  private ApiEditWrapper wrapper;

  @BeforeEach
  void setUp() {
    tagRepository = new TagRepository();
    tokenRepository = new TokenRepository(apiTokens);
    user = new CurrentUserService(knownDefinitions, tokenRepository);
    loginAsBot();
    wrapper = createWrapper(THROTTLE_PERIOD);
  }

  @AfterEach
  void tearDown() {
    wrapper.destroy();
  }

  @DisplayName("Edit sends the CSRF token retrieved from the wiki")
  @Test
  void editWithCsrfToken() {
    givenCsrfToken();
    mockApiEdit.givenEdit(WIKI, editQuery("Summary", List.of(), CSRF_TOKEN)).willReturn(EDIT);

    final Edit edit = wrapper.edit(WIKI, editQuery("Summary", null, null));

    Assertions.assertThat(edit).isEqualTo(EDIT);
  }

  @DisplayName("CSRF token is retrieved only once for several edits")
  @Test
  void editTwiceWithSameCsrfToken() {
    givenCsrfToken();
    mockApiEdit.givenEdit(WIKI, editQuery("First", List.of(), CSRF_TOKEN)).willReturn(EDIT);
    mockApiEdit.givenEdit(WIKI, editQuery("Second", List.of(), CSRF_TOKEN)).willReturn(EDIT);

    wrapper.edit(WIKI, editQuery("First", null, null));
    wrapper.edit(WIKI, editQuery("Second", null, null));
  }

  @DisplayName("CSRF token is retrieved for each wiki")
  @Test
  void editOnTwoWikis() {
    givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit.givenEdit(WIKI, editQuery("Summary", List.of(), CSRF_TOKEN)).willReturn(EDIT);
    givenCsrfToken(OTHER_WIKI, OTHER_CSRF_TOKEN);
    mockApiEdit
        .givenEdit(OTHER_WIKI, editQuery("Summary", List.of(), OTHER_CSRF_TOKEN))
        .willReturn(EDIT);

    wrapper.edit(WIKI, editQuery("Summary", null, null));
    wrapper.edit(OTHER_WIKI, editQuery("Summary", null, null));
  }

  @DisplayName("CSRF token is retrieved again after a new login")
  @Test
  void editAfterNewLogin() {
    givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit.givenEdit(WIKI, editQuery("Summary", List.of(), CSRF_TOKEN)).willReturn(EDIT);
    givenCsrfToken(WIKI, OTHER_CSRF_TOKEN);
    mockApiEdit.givenEdit(WIKI, editQuery("Summary", List.of(), OTHER_CSRF_TOKEN)).willReturn(EDIT);

    wrapper.edit(WIKI, editQuery("Summary", null, null));
    user.logout();
    loginAsBot();
    wrapper.edit(WIKI, editQuery("Summary", null, null));
  }

  @DisplayName("Edits are throttled for a user not logged in")
  @Test
  void editsThrottledForUserNotLoggedIn() {
    wrapper.destroy();
    wrapper = createWrapper(SHORT_THROTTLE_PERIOD);
    user.logout();
    givenCsrfToken();
    mockApiEdit.givenEdit(WIKI, editQuery("First", List.of(), CSRF_TOKEN)).willReturn(EDIT);
    mockApiEdit.givenEdit(WIKI, editQuery("Second", List.of(), CSRF_TOKEN)).willReturn(EDIT);

    final long start = System.nanoTime();
    wrapper.edit(WIKI, editQuery("First", null, null));
    wrapper.edit(WIKI, editQuery("Second", null, null));

    Assertions.assertThat(Duration.ofNanos(System.nanoTime() - start))
        .isGreaterThanOrEqualTo(SHORT_THROTTLE_PERIOD);
  }

  @DisplayName("Edits are not throttled for a bot")
  @Test
  void editsNotThrottledForBot() {
    givenCsrfToken();
    for (int index = 0; index < 5; index++) {
      mockApiEdit.givenEdit(WIKI, editQuery("Summary", List.of(), CSRF_TOKEN)).willReturn(EDIT);
    }

    final long start = System.nanoTime();
    for (int index = 0; index < 5; index++) {
      wrapper.edit(WIKI, editQuery("Summary", null, null));
    }

    Assertions.assertThat(Duration.ofNanos(System.nanoTime() - start))
        .isLessThan(THROTTLE_PERIOD.dividedBy(2));
  }

  @DisplayName("Edit uses the first known tag and the prefix for tagged edits")
  @Test
  void editWithKnownTag() {
    tagRepository.addTag(new Tag(true, true, null, null, null, "wpcleanerng", List.of()));
    wrapper.setTags(List.of("unknown", "WPCleanerNG"));
    wrapper.setPrefixes("WPCleanerNG v1", "v1");
    givenCsrfToken();
    mockApiEdit
        .givenEdit(WIKI, editQuery("v1 - Summary", List.of("wpcleanerng"), CSRF_TOKEN))
        .willReturn(EDIT);

    final Edit edit = wrapper.edit(WIKI, editQuery("Summary", null, null));

    Assertions.assertThat(edit).isEqualTo(EDIT);
  }

  @DisplayName("Edit uses the prefix for untagged edits when no tag is known")
  @Test
  void editWithoutKnownTag() {
    wrapper.setTags(List.of("unknown"));
    wrapper.setPrefixes("WPCleanerNG v1", "v1");
    givenCsrfToken();
    mockApiEdit
        .givenEdit(WIKI, editQuery("WPCleanerNG v1 - Summary", List.of(), CSRF_TOKEN))
        .willReturn(EDIT);

    final Edit edit = wrapper.edit(WIKI, editQuery("Summary", null, null));

    Assertions.assertThat(edit).isEqualTo(EDIT);
  }

  @DisplayName("Edit throws ApiException when MediaWiki returns errors")
  @Test
  void editWithErrors() {
    givenCsrfToken();
    mockApiEdit
        .givenEdit(WIKI, editQuery("Summary", List.of(), CSRF_TOKEN))
        .willReturnErrors(List.of(new ApiError("protectedpage", "edit", "Page is protected.")));

    Assertions.assertThatThrownBy(() -> wrapper.edit(WIKI, editQuery("Summary", null, null)))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception -> {
              Assertions.assertThat(exception).hasMessage("Error editing page with title Test");
              Assertions.assertThat(exception.getDetails())
                  .isEqualTo("protectedpage: Page is protected.");
            });
  }

  @DisplayName("Edit throws ApiException when no CSRF token is retrieved")
  @Test
  void editWithoutCsrfToken() {
    mockApiTokens
        .givenRequestTokens(WIKI, List.of(TokensParameters.Type.CSRF))
        .willReturn(Tokens.ofEmpty());

    Assertions.assertThatThrownBy(() -> wrapper.edit(WIKI, editQuery("Summary", null, null)))
        .isInstanceOf(ApiException.class)
        .hasMessage("Error editing page with title Test")
        .hasRootCauseMessage("Unable to retrieve CSRF token");
  }

  @DisplayName("Move uses the first known tag and the prefix for tagged moves")
  @Test
  void moveWithKnownTag() {
    tagRepository.addTag(new Tag(true, true, null, null, null, "wpcleanerng", List.of()));
    wrapper.setTags(List.of("WPCleanerNG"));
    wrapper.setPrefixes("WPCleanerNG v1", "v1");
    givenCsrfToken();
    mockApiMove
        .givenMove(WIKI, moveQuery("v1 - Drafting", List.of("wpcleanerng"), CSRF_TOKEN))
        .willReturn(MOVE);

    final Move move = wrapper.move(WIKI, moveQuery("Drafting", null, null));

    Assertions.assertThat(move).isEqualTo(MOVE);
  }

  @DisplayName("Move without reason is not prefixed")
  @Test
  void moveWithoutReason() {
    wrapper.setPrefixes("WPCleanerNG v1", "v1");
    givenCsrfToken();
    mockApiMove.givenMove(WIKI, moveQuery(null, List.of(), CSRF_TOKEN)).willReturn(MOVE);

    final Move move = wrapper.move(WIKI, moveQuery(null, null, null));

    Assertions.assertThat(move).isEqualTo(MOVE);
  }

  @DisplayName("Move throws ApiException when MediaWiki server fails")
  @Test
  void moveWhenServerFails() {
    givenCsrfToken();
    mockApiMove.givenMove(WIKI, moveQuery("Drafting", List.of(), CSRF_TOKEN)).willFail();

    Assertions.assertThatThrownBy(() -> wrapper.move(WIKI, moveQuery("Drafting", null, null)))
        .isInstanceOf(ApiException.class)
        .hasMessage("Error moving page from User:Test to User:Test/Draft");
  }

  @DisplayName("Edit is rejected once the wrapper is destroyed")
  @Test
  void editAfterDestroy() {
    wrapper.destroy();

    Assertions.assertThatThrownBy(() -> wrapper.edit(WIKI, editQuery("Summary", null, null)))
        .isInstanceOf(RejectedExecutionException.class);
  }

  private void givenCsrfToken() {
    givenCsrfToken(WIKI, CSRF_TOKEN);
  }

  private void givenCsrfToken(final WikiDefinition wiki, final String csrfToken) {
    mockApiTokens
        .givenRequestTokens(wiki, List.of(TokensParameters.Type.CSRF))
        .willReturn(new Tokens(null, csrfToken, null, null, null, null, null, null, null));
  }

  private void loginAsBot() {
    user.login(WIKI, "Bot", false);
    user.withGroups(List.of("bot"));
  }

  private ApiEditWrapper createWrapper(final Duration throttlePeriod) {
    return new ApiEditWrapper(
        apiEdit, apiMove, tagRepository, tokenRepository, user, throttlePeriod.toSeconds());
  }

  private static EditQuery editQuery(
      final String summary, final List<String> tags, final String token) {
    return new EditQueryByTitle(
        "Test",
        EditQueryCommon.emptyBuilder()
            .summary(summary)
            .text("New text")
            .tags(tags)
            .token(token)
            .build());
  }

  private static MoveQuery moveQuery(
      final String reason, final List<String> tags, final String token) {
    return new MoveQueryByTitle(
        "User:Test",
        MoveQueryCommon.emptyBuilder()
            .to("User:Test/Draft")
            .reason(reason)
            .tags(tags)
            .token(token)
            .build());
  }
}
