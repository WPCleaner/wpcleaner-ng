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
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiTagsTest {

  @Autowired private ApiTags apiTags;
  @Autowired private MockApiTags mockApiTags;

  @DisplayName("Retrieve tags returns the tags given to the mock")
  @Test
  void retrieveTags() {
    final Set<TagsParameters.Properties> properties =
        Set.of(
            TagsParameters.Properties.ACTIVE,
            TagsParameters.Properties.DEFINED,
            TagsParameters.Properties.DESCRIPTION,
            TagsParameters.Properties.DISPLAY_NAME);
    final List<Tag> tags =
        List.of(
            new Tag(
                true,
                true,
                "Edits made with WPCleanerNG",
                "WPCleanerNG",
                12,
                "wpcleanerng",
                List.of("extension")),
            new Tag(false, false, null, null, null, "old-tag", List.of()));
    mockApiTags
        .givenRetrieveTags(WikipediaDefinitions.EN, Limit.max(), "next", properties)
        .willReturn(tags);

    final List<Tag> result =
        apiTags.retrieveTags(WikipediaDefinitions.EN, Limit.max(), "next", properties);

    Assertions.assertThat(result).isEqualTo(tags);
  }

  @DisplayName("Retrieve tags fails when the mock fails")
  @Test
  void retrieveTagsWhenFailing() {
    mockApiTags.givenRetrieveTags(WikipediaDefinitions.EN, null, null, null).willFail();

    Assertions.assertThatThrownBy(
            () -> apiTags.retrieveTags(WikipediaDefinitions.EN, null, null, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
