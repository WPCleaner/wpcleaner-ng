package org.wpcleaner.api.api.query.list.random;

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
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = ApiRandomTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiRandomTest {

  @Autowired private ApiRandom apiRandom;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Retrieve random pages with default options")
  @Test
  void retrieveRandomPagesDefault() {
    final List<RandomPage> randomPages =
        apiRandom.retrieveRandomPages(WikimediaDefinitions.META, null);

    Assertions.assertThat(randomPages).as("randomPages").isNotNull();
    if (!randomPages.isEmpty()) {
      final RandomPage first = randomPages.getFirst();
      Assertions.assertThat(first.title()).as("title").isNotNull();
      Assertions.assertThat(first.id()).as("id").isNotZero();
    }
  }

  @DisplayName("Retrieve random pages with specific limit, namespace, and redirect filter")
  @Test
  void retrieveRandomPagesWithOptions() {
    final RandomQuery options =
        RandomQuery.emptyBuilder()
            .limit(Limit.of(2))
            .namespace(Set.of(new Namespace(0, "Main", "")))
            .filterRedirect(RandomParameters.FilterRedirect.NON_REDIRECTS)
            .build();

    final List<RandomPage> randomPages =
        apiRandom.retrieveRandomPages(WikimediaDefinitions.META, options);

    Assertions.assertThat(randomPages).as("randomPages").isNotNull();
    Assertions.assertThat(randomPages.size()).as("size").isLessThanOrEqualTo(2);
    for (final RandomPage page : randomPages) {
      Assertions.assertThat(page.title()).as("title").isNotNull();
      Assertions.assertThat(page.ns()).as("ns").isZero();
      Assertions.assertThat(page.id()).as("id").isNotZero();
    }
  }
}
