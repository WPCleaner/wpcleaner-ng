package org.wpcleaner.api.hook.login;

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
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = LoginHookTest.SpringBootTestConfig.class)
@TestCallingMWApi
class LoginHookTest {

  @Autowired private LoginHook loginHook;
  @Autowired private TagRepository tagRepository;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Execute login hook and populate TagRepository")
  @Test
  void executeHookPopulatesTagRepository() {
    // GIVEN
    loginHook.addTagsProperties(
        Set.of(
            TagsParameters.Properties.ACTIVE,
            TagsParameters.Properties.DEFINED,
            TagsParameters.Properties.DESCRIPTION,
            TagsParameters.Properties.DISPLAY_NAME));

    // WHEN
    loginHook.executeHook(WikimediaDefinitions.META);

    // THEN
    final List<Tag> tags = tagRepository.getTags();
    Assertions.assertThat(tags).as("tags").isNotEmpty();
    for (final Tag tag : tags) {
      Assertions.assertThat(tag.name()).as("name").isNotNull().isNotEmpty();
      Assertions.assertThat(tag.description()).as("description").isNotNull();
    }
  }
}
