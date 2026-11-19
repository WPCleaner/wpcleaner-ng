package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiCategoryMembersTest {

  private static final String CATEGORY = "Category:Physics";

  @Autowired private ApiCategoryMembers apiCategoryMembers;
  @Autowired private MockApiCategoryMembers mockApiCategoryMembers;

  @DisplayName("Retrieve category members by title returns the members given to the mock")
  @Test
  void retrieveCategoryMembersByTitle() {
    final CategoryMembersQuery options =
        CategoryMembersQuery.emptyBuilder()
            .direction(CategoryMembersParameters.Direction.NEWER)
            .end(Instant.parse("2026-11-18T00:00:00Z"))
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .properties(
                Set.of(
                    CategoryMembersParameters.Properties.TITLE,
                    CategoryMembersParameters.Properties.TIMESTAMP))
            .sort(CategoryMembersParameters.Sort.TIMESTAMP)
            .start(Instant.parse("2026-01-01T00:00:00Z"))
            .type(Set.of(CategoryMembersParameters.Type.PAGE))
            .build();
    final List<CategoryMember> members =
        List.of(
            new CategoryMember(
                0, 22_939, null, null, Instant.parse("2026-06-16T13:17:49Z"), "Physics", "page"),
            new CategoryMember(0, null, null, null, null, "Optics", null));
    mockApiCategoryMembers
        .givenRetrieveCategoryMembersByTitle(WikipediaDefinitions.EN, CATEGORY, options)
        .willReturn(members);

    final List<CategoryMember> result =
        apiCategoryMembers.retrieveCategoryMembersByTitle(
            WikipediaDefinitions.EN, CATEGORY, options);

    Assertions.assertThat(result).isEqualTo(members);
  }

  @DisplayName("Retrieve category members by page id returns the members given to the mock")
  @Test
  void retrieveCategoryMembersByPageId() {
    final CategoryMembersQuery options =
        CategoryMembersQuery.emptyBuilder()
            .endHexSortKey("50")
            .endSortKeyPrefix("P")
            .properties(
                Set.of(
                    CategoryMembersParameters.Properties.SORT_KEY,
                    CategoryMembersParameters.Properties.SORT_KEY_PREFIX))
            .sort(CategoryMembersParameters.Sort.SORT_KEY)
            .startHexSortKey("41")
            .startSortKeyPrefix("A")
            .build();
    final List<CategoryMember> members =
        List.of(new CategoryMember(14, null, "4f50", "Optics", null, null, null));
    mockApiCategoryMembers
        .givenRetrieveCategoryMembersByPageId(WikipediaDefinitions.EN, 691_810, options)
        .willReturn(members);

    final List<CategoryMember> result =
        apiCategoryMembers.retrieveCategoryMembersByPageId(
            WikipediaDefinitions.EN, 691_810, options);

    Assertions.assertThat(result).isEqualTo(members);
  }

  @DisplayName("Retrieve category members fails when the mock fails")
  @Test
  void retrieveCategoryMembersWhenFailing() {
    mockApiCategoryMembers
        .givenRetrieveCategoryMembersByTitle(WikipediaDefinitions.EN, CATEGORY, null)
        .willFail();

    Assertions.assertThatThrownBy(
            () ->
                apiCategoryMembers.retrieveCategoryMembersByTitle(
                    WikipediaDefinitions.EN, CATEGORY, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
