package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.bot.BotSpringBootTest;

@BotSpringBootTest
class ContentFileReaderTest {

  @Autowired private ContentFileReader reader;

  @TempDir private Path folder;

  @DisplayName("should stream the pages of a compressed content file")
  @Test
  void readPages() throws IOException {
    // GIVEN
    final List<ContentPage> pages =
        List.of(
            new ContentPage(3, 0, "Antoine Meillet", "wikitext", "A <ref>b</ref> & [[c]]\nd"),
            new ContentPage(12, 14, "Catégorie:Linguiste", "wikitext", ""),
            new ContentPage(25, 8, "MediaWiki:Common.css", "css", "a { b: c; }"));
    final Path file = folder.resolve("frwiki-2026-11-01-p1p25.xml.bz2");
    ContentFileWriter.write(file, pages);
    final List<ContentPage> readPages = new ArrayList<>();

    // WHEN
    reader.read(file, readPages::add);

    // THEN
    Assertions.assertThat(readPages).isEqualTo(pages);
    Assertions.assertThat(readPages)
        .extracting(ContentPage::isWikitext)
        .containsExactly(true, true, false);
  }
}
