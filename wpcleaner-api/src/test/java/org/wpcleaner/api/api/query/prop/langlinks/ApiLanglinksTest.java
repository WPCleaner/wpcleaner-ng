package org.wpcleaner.api.api.query.prop.langlinks;

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
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@SpringBootTest(classes = ApiLanglinksTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiLanglinksTest {

  @Autowired private ApiLanglinks apiLanglinks;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Retrieve langlinks by title with default options")
  @Test
  void retrieveLanglinksDefault() {
    final List<Page> pages =
        apiLanglinks.retrieveLanglinksByTitle(WikipediaDefinitions.EN, List.of("Main Page"), null);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    final Page page = pages.getFirst();
    Assertions.assertThat(page.title()).as("title").isEqualTo("Main Page");
    Assertions.assertThat(page.langlinks()).as("langlinks").isNotEmpty();
    final Langlink langlink = page.langlinks().getFirst();
    Assertions.assertThat(langlink.lang()).as("lang").isNotNull();
    Assertions.assertThat(langlink.title()).as("title").isNotNull();
  }

  @DisplayName("Retrieve langlinks with all properties and direction specified")
  @Test
  void retrieveLanglinksWithAllProperties() {
    final LanglinksQuery options =
        LanglinksQuery.emptyBuilder()
            .direction(LanglinksParameters.Direction.ASCENDING)
            .inLanguageCode("en")
            .limit(Limit.of(5))
            .properties(
                Set.of(
                    LanglinksParameters.Properties.AUTONYM,
                    LanglinksParameters.Properties.LANGUAGE_NAME,
                    LanglinksParameters.Properties.URL))
            .build();

    final List<Page> pages =
        apiLanglinks.retrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of("Main Page"), options);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    final Page page = pages.getFirst();
    Assertions.assertThat(page.langlinks()).as("langlinks").isNotEmpty();
    final Langlink langlink = page.langlinks().getFirst();
    Assertions.assertThat(langlink.autonym()).as("autonym").isNotNull();
    Assertions.assertThat(langlink.lang()).as("lang").isNotNull();
    Assertions.assertThat(langlink.langName()).as("langName").isNotNull();
    Assertions.assertThat(langlink.title()).as("title").isNotNull();
    Assertions.assertThat(langlink.url()).as("url").isNotNull();
  }

  @DisplayName("Retrieve langlinks filtered by language")
  @Test
  void retrieveLanglinksByLanguage() {
    final LanglinksQuery options =
        LanglinksQuery.emptyBuilder()
            .language("fr")
            .properties(Set.of(LanglinksParameters.Properties.URL))
            .build();

    final List<Page> pages =
        apiLanglinks.retrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of("Main Page"), options);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    final Page page = pages.getFirst();
    Assertions.assertThat(page.langlinks()).as("langlinks").hasSize(1);
    final Langlink langlink = page.langlinks().getFirst();
    Assertions.assertThat(langlink.lang()).as("lang").isEqualTo("fr");
    Assertions.assertThat(langlink.url()).as("url").contains("fr.wikipedia.org");
  }

  @DisplayName("Retrieve langlinks by page ID")
  @Test
  void retrieveLanglinksByPageId() {
    final List<Page> pagesByTitle =
        apiLanglinks.retrieveLanglinksByTitle(WikipediaDefinitions.EN, List.of("Main Page"), null);
    Assertions.assertThat(pagesByTitle).isNotEmpty();
    final Integer pageId = pagesByTitle.getFirst().pageId();
    Assertions.assertThat(pageId).isNotNull();

    final List<Page> pagesById =
        apiLanglinks.retrieveLanglinksByPageId(WikipediaDefinitions.EN, List.of(pageId), null);
    Assertions.assertThat(pagesById).isNotEmpty();
    final Page pageById = pagesById.getFirst();
    Assertions.assertThat(pageById.pageId()).isEqualTo(pageId);
    Assertions.assertThat(pageById.title()).isEqualTo("Main Page");
  }

  @DisplayName("Retrieve langlinks for a missing page")
  @Test
  void retrieveLanglinksMissingPage() {
    final List<Page> pages =
        apiLanglinks.retrieveLanglinksByTitle(
            WikipediaDefinitions.EN, List.of("NonExistentPageTitle123456789WPCleanerTest"), null);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    final Page page = pages.getFirst();
    Assertions.assertThat(page.missing()).as("missing").isTrue();
    Assertions.assertThat(page.langlinks()).as("langlinks").isEmpty();
  }
}
