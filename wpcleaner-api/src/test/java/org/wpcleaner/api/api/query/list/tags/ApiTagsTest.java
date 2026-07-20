package org.wpcleaner.api.api.query.list.tags;

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

@SpringBootTest(classes = ApiTagsTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiTagsTest {

  @Autowired private ApiTags apiTags;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Retrieve tags with default options")
  @Test
  void retrieveTagsDefault() {
    // WHEN
    final List<Tag> tags = apiTags.retrieveTags(WikimediaDefinitions.META, null, null, null);

    // THEN
    Assertions.assertThat(tags).as("tags").isNotNull();
    if (!tags.isEmpty()) {
      final Tag first = tags.getFirst();
      Assertions.assertThat(first.name()).as("name").isNotNull().isNotEmpty();
    }
  }

  @DisplayName("Retrieve tags with specific limit")
  @Test
  void retrieveTagsWithLimit() {
    // WHEN
    final List<Tag> tags = apiTags.retrieveTags(WikimediaDefinitions.META, "5", null, null);

    // THEN
    Assertions.assertThat(tags).as("tags").isNotNull();
    Assertions.assertThat(tags.size()).as("size").isLessThanOrEqualTo(5);
    for (final Tag tag : tags) {
      Assertions.assertThat(tag.name()).as("name").isNotNull().isNotEmpty();
    }
  }

  @DisplayName("Retrieve tags with specific properties")
  @Test
  void retrieveTagsWithProperties() {
    // GIVEN
    final Set<TagsParameters.Properties> properties =
        Set.of(
            TagsParameters.Properties.ACTIVE,
            TagsParameters.Properties.DEFINED,
            TagsParameters.Properties.DESCRIPTION,
            TagsParameters.Properties.DISPLAY_NAME,
            TagsParameters.Properties.HIT_COUNT,
            TagsParameters.Properties.SOURCE);

    // WHEN
    final List<Tag> tags = apiTags.retrieveTags(WikimediaDefinitions.META, "5", null, properties);

    // THEN
    Assertions.assertThat(tags).as("tags").isNotNull();
    Assertions.assertThat(tags.size()).as("size").isLessThanOrEqualTo(5);
    for (final Tag tag : tags) {
      Assertions.assertThat(tag.name()).as("name").isNotNull().isNotEmpty();
      Assertions.assertThat(tag.description()).as("description").isNotNull();
      Assertions.assertThat(tag.hitCount()).as("hitCount").isNotNull().isGreaterThanOrEqualTo(0);
      Assertions.assertThat(tag.source()).as("source").isNotNull();
    }
  }
}
