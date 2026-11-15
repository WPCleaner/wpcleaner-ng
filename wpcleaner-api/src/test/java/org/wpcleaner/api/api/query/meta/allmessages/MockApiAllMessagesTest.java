package org.wpcleaner.api.api.query.meta.allmessages;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiAllMessagesTest {

  @Autowired private ApiAllMessages apiAllMessages;
  @Autowired private MockApiAllMessages mockApiAllMessages;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() {
    mediaWikiServer.reset();
  }

  @DisplayName("Retrieve messages with every parameter returns the messages given to the mock")
  @Test
  void retrieveMessages() {
    final AllMessagesQuery query =
        AllMessagesQuery.emptyBuilder()
            .args(List.of("b", "a"))
            .customised(AllMessagesParameters.Customised.MODIFIED)
            .enableParser(true)
            .filter("tag")
            .from("linter-category-a")
            .includeLocal(true)
            .language("fr")
            .messages(Set.of("linter-category-obsolete-tag", "mainpage"))
            .noContent(true)
            .prefix("linter-category-")
            .properties(Set.of(AllMessagesParameters.Properties.DEFAULT))
            .title("Paris")
            .to("linter-category-z")
            .build();
    final List<Message> messages =
        List.of(
            new Message(
                "Balises HTML obsolètes",
                true,
                null,
                "Obsolete HTML tags",
                null,
                "linter-category-obsolete-tag",
                "linter-category-obsolete-tag"),
            new Message(null, null, true, null, true, "Mainpage", "mainpage"));
    mockApiAllMessages.givenRetrieveMessages(WikipediaDefinitions.FR, query).willReturn(messages);

    final List<Message> result = apiAllMessages.retrieveMessages(WikipediaDefinitions.FR, query);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(messages);
  }

  @DisplayName("Retrieve messages returns no message when the answer has no query")
  @Test
  void retrieveMessagesWithoutQuery() {
    final AllMessagesQuery query = AllMessagesQuery.emptyBuilder().prefix("linter-").build();
    mockApiAllMessages
        .givenRetrieveMessages(WikipediaDefinitions.FR, query)
        .answer()
        .willReturn(Map.of("batchcomplete", true));

    final List<Message> result = apiAllMessages.retrieveMessages(WikipediaDefinitions.FR, query);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEmpty();
  }

  @DisplayName("Retrieve messages fails when the API returns errors")
  @Test
  void retrieveMessagesWithErrors() {
    final AllMessagesQuery query = AllMessagesQuery.emptyBuilder().prefix("linter-").build();
    mockApiAllMessages
        .givenRetrieveMessages(WikipediaDefinitions.FR, query)
        .willReturnErrors(List.of(new ApiError("badvalue", "main", "Bad value.")));

    Assertions.assertThatThrownBy(
            () -> apiAllMessages.retrieveMessages(WikipediaDefinitions.FR, query))
        .isInstanceOf(ApiException.class);
  }
}
