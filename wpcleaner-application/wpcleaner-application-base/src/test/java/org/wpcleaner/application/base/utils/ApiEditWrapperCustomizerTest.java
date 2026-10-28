package org.wpcleaner.application.base.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.edit.ApiEditWrapper;
import org.wpcleaner.api.api.edit.Edit;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.api.edit.MockApiEdit;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiEditWrapperCustomizerTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token";
  private static final Edit EDIT = new Edit(Edit.RESULT_SUCCESS, 1, "Test", null);

  @Autowired private ApiEditWrapper apiEditWrapper;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiTokens mockApiTokens;
  @Autowired private TagRepository tagRepository;

  @Value("${version.prefix}")
  private String versionPrefix;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    currentUserService.login(WIKI, "Bot", false);
    currentUserService.withGroups(List.of("bot"));
  }

  @AfterEach
  void tearDown() {
    currentUserService.logout();
  }

  @DisplayName("ApiEditWrapperCustomizer configures the summary prefixes and the tags of edits")
  @Test
  void customizedEdits() {
    Assertions.assertThat(versionPrefix).isNotBlank();
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit
        .givenEdit(
            WIKI,
            editQuery("WPCleanerNG v%s - Summary".formatted(versionPrefix), List.of(), CSRF_TOKEN))
        .willReturn(EDIT);
    mockApiEdit
        .givenEdit(
            WIKI,
            editQuery("v%s - Summary".formatted(versionPrefix), List.of("wpcleaner"), CSRF_TOKEN))
        .willReturn(EDIT);

    apiEditWrapper.edit(WIKI, editQuery("Summary", null, null));
    tagRepository.addTag(new Tag(true, true, null, null, null, "wpcleaner", List.of()));
    apiEditWrapper.edit(WIKI, editQuery("Summary", null, null));
  }

  private static EditQueryByTitle editQuery(
      final String summary, final List<String> tags, final String token) {
    return new EditQueryByTitle(
        "Test",
        EditQueryCommon.emptyBuilder()
            .summary(summary)
            .text("Text")
            .tags(tags)
            .token(token)
            .build());
  }
}
