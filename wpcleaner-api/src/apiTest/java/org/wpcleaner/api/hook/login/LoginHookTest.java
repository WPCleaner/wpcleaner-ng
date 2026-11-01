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
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.list.tags.TagsParameters;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class LoginHookTest {

  @Autowired private ExtensionTagRepository extensionTagRepository;
  @Autowired private InterwikiRepository interwikiRepository;
  @Autowired private LoginHook loginHook;
  @Autowired private ProtocolRepository protocolRepository;
  @Autowired private TagRepository tagRepository;

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
    loginHook.executeHook(WikimediaDefinitions.META, new ProgressTracker(_ -> {}));

    // THEN
    final List<Tag> tags = tagRepository.getTags(WikimediaDefinitions.META);
    Assertions.assertThat(tags).as("tags").isNotEmpty();
    for (final Tag tag : tags) {
      Assertions.assertThat(tag.name()).as("name").isNotNull().isNotEmpty();
      Assertions.assertThat(tag.description()).as("description").isNotNull();
    }
  }

  @DisplayName("Execute login hook and populate InterwikiRepository")
  @Test
  void executeHookPopulatesInterwikiRepository() {
    // WHEN
    loginHook.executeHook(WikimediaDefinitions.META, new ProgressTracker(_ -> {}));

    // THEN
    final List<Interwiki> interwikis = interwikiRepository.getInterwikis(WikimediaDefinitions.META);
    Assertions.assertThat(interwikis).as("interwikis").isNotEmpty();
    for (final Interwiki interwiki : interwikis) {
      Assertions.assertThat(interwiki.prefix()).as("prefix").isNotNull().isNotEmpty();
      Assertions.assertThat(interwiki.url()).as("url").isNotNull().isNotEmpty();
    }
  }

  @DisplayName("Execute login hook and populate ProtocolRepository")
  @Test
  void executeHookPopulatesProtocolRepository() {
    // WHEN
    loginHook.executeHook(WikimediaDefinitions.META, new ProgressTracker(_ -> {}));

    // THEN
    final List<Protocol> protocols = protocolRepository.getProtocols(WikimediaDefinitions.META);
    Assertions.assertThat(protocols).as("protocols").isNotEmpty();
    for (final Protocol protocol : protocols) {
      Assertions.assertThat(protocol.value()).as("value").isNotNull().isNotEmpty();
    }
  }

  @DisplayName("Execute login hook and populate ExtensionTagRepository")
  @Test
  void executeHookPopulatesExtensionTagRepository() {
    // WHEN
    loginHook.executeHook(WikimediaDefinitions.META, new ProgressTracker(_ -> {}));

    // THEN
    final List<ExtensionTag> extensionTags =
        extensionTagRepository.getExtensionTags(WikimediaDefinitions.META);
    Assertions.assertThat(extensionTags).as("extensionTags").contains(new ExtensionTag("nowiki"));
    for (final ExtensionTag extensionTag : extensionTags) {
      Assertions.assertThat(extensionTag.name()).as("name").isNotEmpty().doesNotContain("<", ">");
    }
  }
}
