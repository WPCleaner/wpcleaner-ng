package org.wpcleaner.api.api.query.prop.revisions;

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

@SpringBootTest(classes = ApiRevisionsTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiRevisionsTest {

  @Autowired private ApiRevisions apiRevisions;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Retrieve revisions by title with default options")
  @Test
  void retrieveRevisionsDefault() {
    // WHEN
    final List<Page> pages =
        apiRevisions.retrieveRevisionsByTitle(
            WikimediaDefinitions.META, List.of("Main Page"), null);

    // THEN
    Assertions.assertThat(pages).as("pages").isNotNull();
    if (!pages.isEmpty()) {
      final Page page = pages.getFirst();
      Assertions.assertThat(page.title()).as("title").isEqualTo("Main Page");
      final List<Revision> revisions = page.revisions();
      Assertions.assertThat(revisions).as("revisions").isNotNull();
      if (!revisions.isEmpty()) {
        final Revision revision = revisions.getFirst();
        Assertions.assertThat(revision.revId()).as("revId").isNotNull();
      }
    }
  }

  @DisplayName("Retrieve revisions by revision ID")
  @Test
  void retrieveRevisionsByRevisionIdTest() {
    final List<Page> pagesByTitle =
        apiRevisions.retrieveRevisionsByTitle(
            WikimediaDefinitions.META, List.of("Main Page"), null);
    Assertions.assertThat(pagesByTitle).isNotEmpty();
    final Page page = pagesByTitle.getFirst();
    Assertions.assertThat(page.revisions()).isNotEmpty();
    final Integer revId = page.revisions().getFirst().revId();
    Assertions.assertThat(revId).isNotNull();

    final List<Page> pagesById =
        apiRevisions.retrieveRevisionsByRevisionId(WikimediaDefinitions.META, List.of(revId), null);

    Assertions.assertThat(pagesById).isNotEmpty();
    final Page pageById = pagesById.getFirst();
    Assertions.assertThat(pageById.revisions()).isNotEmpty();
    Assertions.assertThat(pageById.revisions().getFirst().revId()).isEqualTo(revId);

    // Test with an invalid/zero old revision ID
    final List<Page> pagesWithInvalid =
        apiRevisions.retrieveRevisionsByRevisionId(
            WikimediaDefinitions.META, List.of(0, revId), null);
    Assertions.assertThat(pagesWithInvalid).isNotNull();
    final boolean hasRevId =
        pagesWithInvalid.stream()
            .flatMap(p -> p.revisions().stream())
            .anyMatch(revision -> revId.equals(revision.revId()));
    Assertions.assertThat(hasRevId)
        .as("Valid revId should still be found when queried with 0")
        .isTrue();
  }

  @DisplayName("Retrieve revisions by title with all properties and slots specified")
  @Test
  void retrieveRevisionsWithAllProperties() {
    // GIVEN
    final RevisionsQuery options =
        RevisionsQuery.emptyBuilder()
            .limit(1)
            .properties(
                Set.of(
                    RevisionsParameters.Properties.COMMENT,
                    RevisionsParameters.Properties.CONTENT,
                    RevisionsParameters.Properties.CONTENT_MODEL,
                    RevisionsParameters.Properties.FLAGS,
                    RevisionsParameters.Properties.IDS,
                    RevisionsParameters.Properties.PARSED_COMMENT,
                    RevisionsParameters.Properties.ROLES,
                    RevisionsParameters.Properties.SHA1,
                    RevisionsParameters.Properties.SIZE,
                    RevisionsParameters.Properties.SLOT_SHA1,
                    RevisionsParameters.Properties.SLOT_SIZE,
                    RevisionsParameters.Properties.TAGS,
                    RevisionsParameters.Properties.TIMESTAMP,
                    RevisionsParameters.Properties.USER,
                    RevisionsParameters.Properties.USER_ID))
            .slots(Set.of("main"))
            .build();

    // WHEN
    final List<Page> pages =
        apiRevisions.retrieveRevisionsByTitle(
            WikimediaDefinitions.META, List.of("Main Page"), options);

    // THEN
    Assertions.assertThat(pages).as("pages").isNotNull();
    if (!pages.isEmpty()) {
      final Page page = pages.getFirst();
      Assertions.assertThat(page.title()).as("title").isEqualTo("Main Page");
      final List<Revision> revisions = page.revisions();
      Assertions.assertThat(revisions).as("revisions").isNotEmpty();
      final Revision revision = revisions.getFirst();
      Assertions.assertThat(revision.revId()).as("revId").isNotNull();
      Assertions.assertThat(revision.timestamp()).as("timestamp").isNotNull();
      Assertions.assertThat(revision.user()).as("user").isNotNull();
      Assertions.assertThat(revision.userId()).as("userId").isNotNull();

      // Slots
      Assertions.assertThat(revision.slots()).as("slots").containsKey("main");
      final RevisionSlot mainSlot = revision.slots().get("main");
      Assertions.assertThat(mainSlot).as("mainSlot").isNotNull();
      Assertions.assertThat(mainSlot.content()).as("content").isNotNull();
      Assertions.assertThat(mainSlot.contentModel()).as("contentModel").isNotNull();
      Assertions.assertThat(mainSlot.sha1()).as("sha1").isNotNull();
      Assertions.assertThat(mainSlot.size()).as("size").isNotNull();
    }
  }
}
