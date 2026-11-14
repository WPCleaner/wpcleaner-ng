package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public final class ContentFileWriter {

  private ContentFileWriter() {}

  public static void write(final Path file, final List<ContentPage> pages) throws IOException {
    final StringBuilder xml =
        new StringBuilder(
            """
            <mediawiki xmlns="http://www.mediawiki.org/xml/export-0.11/" version="0.11">
              <siteinfo>
                <dbname>frwiki</dbname>
                <namespaces>
                  <namespace key="0" case="first-letter" />
                </namespaces>
              </siteinfo>
            """);
    for (final ContentPage page : pages) {
      xml.append(
          """
            <page>
              <title>%s</title>
              <ns>%d</ns>
              <id>%d</id>
              <revision>
                <id>%d</id>
                <timestamp>2026-10-18T03:45:29Z</timestamp>
                <contributor>
                  <username>Somebody</username>
                  <id>3662548</id>
                </contributor>
                <model>%s</model>
                <format>text/x-wiki</format>
                <text bytes="%d" xml:space="preserve">%s</text>
              </revision>
            </page>
          """
              .formatted(
                  escape(page.title()),
                  page.namespace(),
                  page.id(),
                  page.id() * 100,
                  page.model(),
                  page.text().length(),
                  escape(page.text())));
    }
    xml.append("</mediawiki>\n");
    try (OutputStream output = new BZip2CompressorOutputStream(Files.newOutputStream(file))) {
      output.write(xml.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  private static String escape(final String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
