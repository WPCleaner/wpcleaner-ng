package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiCategoryMembersTest {

  private static final String CATEGORY = "Category:Wikimania 2017";
  private static final int CATEGORY_PAGE_ID = 5_667_202;

  @Autowired private ApiCategoryMembers apiCategoryMembers;

  @DisplayName("Retrieve category members by title with default options")
  @Test
  void retrieveCategoryMembersByTitleDefault() {
    final List<CategoryMember> members =
        apiCategoryMembers.retrieveCategoryMembersByTitle(
            WikimediaDefinitions.META, CATEGORY, null);

    Assertions.assertThat(members).as("members").isNotEmpty();
    for (final CategoryMember member : members) {
      Assertions.assertThat(member.title()).as("title").isNotBlank();
      Assertions.assertThat(member.pageId()).as("pageId").isNotNull();
    }
  }

  @DisplayName("Retrieve category members by page id")
  @Test
  void retrieveCategoryMembersByPageId() {
    final List<CategoryMember> byPageId =
        apiCategoryMembers.retrieveCategoryMembersByPageId(
            WikimediaDefinitions.META, CATEGORY_PAGE_ID, null);
    final List<CategoryMember> byTitle =
        apiCategoryMembers.retrieveCategoryMembersByTitle(
            WikimediaDefinitions.META, CATEGORY, null);

    Assertions.assertThat(byPageId).as("members").isNotEmpty().isEqualTo(byTitle);
  }

  @DisplayName("Retrieve category members following continuation")
  @Test
  void retrieveCategoryMembersWithContinuation() {
    final CategoryMembersQuery options =
        CategoryMembersQuery.emptyBuilder().limit(Limit.of(2)).build();

    final List<CategoryMember> members =
        apiCategoryMembers.retrieveCategoryMembersByTitle(
            WikimediaDefinitions.META, CATEGORY, options);

    Assertions.assertThat(members).as("members").hasSizeGreaterThan(2);
    Assertions.assertThat(members)
        .extracting(CategoryMember::title)
        .as("titles")
        .doesNotHaveDuplicates();
  }

  @DisplayName("Retrieve category members of one type with every property")
  @Test
  void retrieveCategoryMembersWithTypeAndProperties() {
    final CategoryMembersQuery options =
        CategoryMembersQuery.emptyBuilder()
            .limit(Limit.max())
            .properties(EnumSet.allOf(CategoryMembersParameters.Properties.class))
            .type(Set.of(CategoryMembersParameters.Type.SUBCATEGORY))
            .build();

    final List<CategoryMember> members =
        apiCategoryMembers.retrieveCategoryMembersByTitle(
            WikimediaDefinitions.META, CATEGORY, options);

    Assertions.assertThat(members).as("members").isNotEmpty();
    for (final CategoryMember member : members) {
      Assertions.assertThat(member.ns()).as("ns").isEqualTo(14);
      Assertions.assertThat(member.pageId()).as("pageId").isNotNull();
      Assertions.assertThat(member.sortKey()).as("sortKey").isNotNull();
      Assertions.assertThat(member.sortKeyPrefix()).as("sortKeyPrefix").isNotNull();
      Assertions.assertThat(member.timestamp()).as("timestamp").isNotNull();
      Assertions.assertThat(member.title()).as("title").startsWith("Category:");
      Assertions.assertThat(member.type()).as("type").isEqualTo("subcat");
    }
  }
}
