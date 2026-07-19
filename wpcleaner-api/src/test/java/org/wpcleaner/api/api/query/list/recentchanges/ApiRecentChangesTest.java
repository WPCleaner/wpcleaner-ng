package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.wpcleaner.api.TestCallingMWApi;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = ApiRecentChangesTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiRecentChangesTest {

  @Autowired private ApiRecentChanges apiRecentChanges;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Retrieve recent changes with default options")
  @Test
  void retrieveRecentChangesDefault() {
    // WHEN
    final List<RecentChange> recentChanges =
        apiRecentChanges.retrieveRecentChanges(WikimediaDefinitions.META, null);

    // THEN
    Assertions.assertThat(recentChanges).as("recentChanges").isNotNull();
    if (!recentChanges.isEmpty()) {
      final RecentChange first = recentChanges.getFirst();
      Assertions.assertThat(first.title()).as("title").isNotNull();
      Assertions.assertThat(first.timestamp()).as("timestamp").isNotNull();
    }
  }

  @DisplayName("Retrieve recent changes with specific limit and properties")
  @Test
  void retrieveRecentChangesWithLimit() {
    // GIVEN
    final RecentChangesQuery options =
        RecentChangesQuery.emptyBuilder()
            .limit(5)
            .properties(
                Set.of(
                    RecentChangesParameters.Properties.COMMENT,
                    RecentChangesParameters.Properties.FLAGS,
                    RecentChangesParameters.Properties.TITLE,
                    RecentChangesParameters.Properties.TIMESTAMP,
                    RecentChangesParameters.Properties.USER,
                    RecentChangesParameters.Properties.USER_ID))
            .build();

    // WHEN
    final List<RecentChange> recentChanges =
        apiRecentChanges.retrieveRecentChanges(WikimediaDefinitions.META, options);

    // THEN
    Assertions.assertThat(recentChanges).as("recentChanges").isNotNull();
    Assertions.assertThat(recentChanges.size()).as("size").isLessThanOrEqualTo(5);
    for (final RecentChange rc : recentChanges) {
      Assertions.assertThat(rc.title()).as("title").isNotNull();
      Assertions.assertThat(rc.user()).as("user").isNotNull();
      Assertions.assertThat(rc.userid()).as("userid").isNotNull();
    }
  }
}
