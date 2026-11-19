package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.categorymembers.ApiCategoryMembers;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMember;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMembersParameters;
import org.wpcleaner.api.api.query.list.categorymembers.CategoryMembersQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record CategoryMembersTask(
    ApiCategoryMembers apiCategoryMembers, WikiDefinition wiki, String categoryTitle)
    implements LongRunningTask<List<String>> {

  @Override
  public List<String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving members of %s", categoryTitle))) {
      final CategoryMembersQuery options =
          CategoryMembersQuery.emptyBuilder()
              .limit(Limit.max())
              .type(
                  Set.of(CategoryMembersParameters.Type.FILE, CategoryMembersParameters.Type.PAGE))
              .build();
      return apiCategoryMembers
          .retrieveCategoryMembersByTitle(wiki, categoryTitle, options)
          .stream()
          .sorted(
              Comparator.comparingInt(CategoryMember::ns)
                  .thenComparing(
                      CategoryMember::title, Comparator.nullsLast(Comparator.naturalOrder())))
          .flatMap(member -> Stream.ofNullable(member.title()))
          .distinct()
          .toList();
    }
  }
}
