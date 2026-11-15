package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.query.meta.allmessages.AllMessagesQuery;
import org.wpcleaner.api.api.query.meta.allmessages.Message;
import org.wpcleaner.api.api.query.meta.allmessages.MockApiAllMessages;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.repository.linter.LinterCategoryRepository;
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class LinterCategoryLabelLoaderTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private LinterCategoryLabelLoader loader;
  @Autowired private LinterCategoryRepository repository;
  @Autowired private MockApiAllMessages mockApiAllMessages;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() {
    repository.logout(new ConnectedUser(false, WIKI, "", false, List.of(), List.of()));
    mediaWikiServer.reset();
  }

  @DisplayName("Should retrieve the labels of the Linter categories of the wiki")
  @Test
  void execute() {
    // GIVEN
    repository.setCategories(
        WIKI,
        List.of(
            new LinterCategory("obsolete-tag", LinterPriority.LOW, null),
            new LinterCategory("missing-end-tag", LinterPriority.LOW, null),
            new LinterCategory("fostered", LinterPriority.MEDIUM, null)));
    mockApiAllMessages
        .givenRetrieveMessages(
            WIKI,
            AllMessagesQuery.emptyBuilder()
                .prefix(LinterCategoryLabelLoader.MESSAGE_PREFIX)
                .build())
        .willReturn(
            List.of(
                createMessage("linter-category-obsolete-tag", "Balises HTML obsolètes"),
                createMessage("linter-category-obsolete-tag-desc", "Ces pages utilisent…"),
                createMessage("linter-category-missing-end-tag", null)));

    // WHEN
    final List<LoginWarning> warnings = loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(warnings).isEmpty();
    Assertions.assertThat(
            List.of("obsolete-tag", "missing-end-tag", "fostered").stream()
                .map(name -> repository.findCategory(WIKI, name))
                .toList())
        .containsExactly(
            Optional.of(
                new LinterCategory("obsolete-tag", LinterPriority.LOW, "Balises HTML obsolètes")),
            Optional.of(new LinterCategory("missing-end-tag", LinterPriority.LOW, null)),
            Optional.of(new LinterCategory("fostered", LinterPriority.MEDIUM, null)));
  }

  @DisplayName("Should not retrieve labels for a wiki without Linter categories")
  @Test
  void executeWithoutCategories() {
    // GIVEN
    repository.setCategories(
        WikipediaDefinitions.EN,
        List.of(new LinterCategory("obsolete-tag", LinterPriority.LOW, null)));

    // WHEN
    final List<LoginWarning> warnings = loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(warnings).isEmpty();
    Assertions.assertThat(repository.findCategory(WIKI, "obsolete-tag")).isEmpty();
  }

  private static Message createMessage(final String name, @Nullable final String content) {
    return new Message(content, null, null, null, content == null ? true : null, name, name);
  }
}
