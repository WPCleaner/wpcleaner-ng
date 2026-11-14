package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ContentFileReader {

  private static final int BUFFER_SIZE = 1 << 20;
  private static final String NO_LIMIT = "0";
  private static final String PAGE = "page";
  private static final String TITLE = "title";
  private static final String NAMESPACE = "ns";
  private static final String ID = "id";
  private static final String REVISION = "revision";
  private static final String MODEL = "model";
  private static final String TEXT = "text";

  private final XMLInputFactory factory;

  public ContentFileReader() {
    factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty("jdk.xml.totalEntitySizeLimit", NO_LIMIT);
    factory.setProperty("jdk.xml.maxGeneralEntitySizeLimit", NO_LIMIT);
  }

  public void read(final Path file, final Consumer<ContentPage> consumer) throws IOException {
    try (InputStream input = open(file)) {
      final XMLStreamReader reader = factory.createXMLStreamReader(input);
      try {
        readPages(reader, consumer);
      } finally {
        reader.close();
      }
    } catch (final XMLStreamException e) {
      throw new IOException("Error reading %s: %s".formatted(file, e.getMessage()), e);
    }
  }

  private static InputStream open(final Path file) throws IOException {
    final InputStream input = new BufferedInputStream(Files.newInputStream(file), BUFFER_SIZE);
    final String name = String.valueOf(file.getFileName());
    if (name.endsWith(".bz2")) {
      return new BufferedInputStream(new BZip2CompressorInputStream(input, true), BUFFER_SIZE);
    }
    if (name.endsWith(".gz")) {
      return new BufferedInputStream(new GZIPInputStream(input, BUFFER_SIZE), BUFFER_SIZE);
    }
    return input;
  }

  private static void readPages(final XMLStreamReader reader, final Consumer<ContentPage> consumer)
      throws XMLStreamException {
    while (reader.hasNext()) {
      if (reader.next() == XMLStreamConstants.START_ELEMENT && PAGE.equals(reader.getLocalName())) {
        consumer.accept(readPage(reader));
      }
    }
  }

  private static ContentPage readPage(final XMLStreamReader reader) throws XMLStreamException {
    final PageParser parser = new PageParser();
    while (parser.isInPage()) {
      final int event = reader.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        parser.start(reader);
      } else if (event == XMLStreamConstants.END_ELEMENT) {
        parser.end(reader.getLocalName());
      }
    }
    return parser.build();
  }

  private static final class PageParser {

    private int depth = 1;
    private boolean inRevision;
    private int id;
    private int namespace;
    @Nullable private String title;
    @Nullable private String model;
    @Nullable private String text;

    private boolean isInPage() {
      return depth > 0;
    }

    private void start(final XMLStreamReader reader) throws XMLStreamException {
      final String name = reader.getLocalName();
      switch (name) {
        case TITLE -> title = reader.getElementText();
        case NAMESPACE -> namespace = Integer.parseInt(reader.getElementText().strip());
        case ID -> readId(reader.getElementText());
        case MODEL -> model = reader.getElementText();
        case TEXT -> text = reader.getElementText();
        default -> {
          depth++;
          inRevision = inRevision || REVISION.equals(name);
        }
      }
    }

    private void end(final String name) {
      depth--;
      inRevision = inRevision && !REVISION.equals(name);
    }

    private void readId(final String value) {
      if (!inRevision) {
        id = Integer.parseInt(value.strip());
      }
    }

    private ContentPage build() {
      return new ContentPage(
          id,
          namespace,
          title == null ? "" : title,
          model == null ? ContentPage.WIKITEXT_MODEL : model,
          text == null ? "" : text);
    }
  }
}
