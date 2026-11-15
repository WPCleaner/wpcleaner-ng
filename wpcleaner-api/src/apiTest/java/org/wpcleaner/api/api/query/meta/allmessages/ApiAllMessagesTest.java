package org.wpcleaner.api.api.query.meta.allmessages;

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
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiAllMessagesTest {

  @Autowired private ApiAllMessages apiAllMessages;

  @DisplayName("Retrieve given messages in a language, with their default content")
  @Test
  void retrieveMessages() {
    final List<Message> messages =
        apiAllMessages.retrieveMessages(
            WikipediaDefinitions.FR,
            AllMessagesQuery.emptyBuilder()
                .messages(Set.of("linter-category-obsolete-tag", "wpcleanerng-missing-message"))
                .language("fr")
                .properties(Set.of(AllMessagesParameters.Properties.DEFAULT))
                .build());

    Assertions.assertThat(messages)
        .extracting(Message::name, Message::missing)
        .containsExactlyInAnyOrder(
            Assertions.tuple("linter-category-obsolete-tag", null),
            Assertions.tuple("wpcleanerng-missing-message", true));
    Assertions.assertThat(messages)
        .filteredOn(message -> message.missing() == null)
        .extracting(Message::content)
        .containsExactly("Balises HTML obsolètes");
  }

  @DisplayName("Retrieve all messages with a prefix")
  @Test
  void retrieveMessagesWithPrefix() {
    final List<Message> messages =
        apiAllMessages.retrieveMessages(
            WikipediaDefinitions.FR,
            AllMessagesQuery.emptyBuilder().prefix("linter-category-").language("en").build());

    Assertions.assertThat(messages)
        .isNotEmpty()
        .allSatisfy(message -> Assertions.assertThat(message.name()).startsWith("linter-category-"))
        .extracting(Message::name, Message::content)
        .contains(Assertions.tuple("linter-category-missing-end-tag", "Missing end tag"));
  }

  @DisplayName("Retrieve a message with arguments")
  @Test
  void retrieveMessagesWithArguments() {
    final List<Message> messages =
        apiAllMessages.retrieveMessages(
            WikipediaDefinitions.EN,
            AllMessagesQuery.emptyBuilder()
                .messages(Set.of("movepage-max-pages"))
                .args(List.of("5"))
                .enableParser(true)
                .language("en")
                .build());

    Assertions.assertThat(messages)
        .singleElement()
        .extracting(Message::content)
        .asString()
        .contains("5 pages");
  }

  @DisplayName("Retrieve customised messages without their content")
  @Test
  void retrieveCustomisedMessagesWithoutContent() {
    final List<Message> messages =
        apiAllMessages.retrieveMessages(
            WikipediaDefinitions.FR,
            AllMessagesQuery.emptyBuilder()
                .messages(Set.of("mainpage", "edit"))
                .customised(AllMessagesParameters.Customised.MODIFIED)
                .noContent(true)
                .build());

    Assertions.assertThat(messages)
        .singleElement()
        .satisfies(
            message -> {
              Assertions.assertThat(message.name()).isEqualTo("mainpage");
              Assertions.assertThat(message.customised()).isTrue();
              Assertions.assertThat(message.content()).isNull();
            });
  }
}
