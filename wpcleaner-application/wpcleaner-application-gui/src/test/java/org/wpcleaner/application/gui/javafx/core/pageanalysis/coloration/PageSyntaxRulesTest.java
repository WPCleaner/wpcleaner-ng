package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.fxmisc.richtext.model.StyleSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

class PageSyntaxRulesTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String NO_STYLE = "-";

  private final JavaFxStylePropertiesRegistry styleRegistry =
      new JavaFxStylePropertiesRegistry(
          new StylePropertiesRegistry(List.of(new PageAnalysisStylePropertiesInitializer())));
  private final PageSyntaxColorizer colorizer =
      new PageSyntaxColorizer(
          List.of(
              new CategorySyntaxRule(),
              new CommentSyntaxRule(),
              new ExternalLinkSyntaxRule(),
              new ImageSyntaxRule(),
              new InternalLinkSyntaxRule(),
              new InterwikiLinkSyntaxRule(),
              new LanguageLinkSyntaxRule(),
              new ListItemSyntaxRule(),
              new MagicLinkSyntaxRule(),
              new ProgrammingSyntaxRule(),
              new ReferenceSyntaxRule(),
              new TableSyntaxRule(),
              new TagSyntaxRule(),
              new TemplateSyntaxRule(),
              new TitleSyntaxRule()),
          styleRegistry);

  @DisplayName("computeStyleSpans colors nested elements with the innermost style")
  @Test
  void colorNestedElements() {
    // GIVEN
    final PageAnalysis analysis = analyze("<ref>a{{T|[[A]]}}</ref>");

    // WHEN
    final List<String> spans = describeSpans(analysis);

    // THEN
    Assertions.assertThat(spans)
        .containsExactly(
            "TAG:5", "REFERENCE:1", "TEMPLATE:4", "INTERNAL_LINK:5", "TEMPLATE:2", "TAG:6");
  }

  @DisplayName("computeStyleSpans colors titles, list items, programming elements and images")
  @Test
  void colorBlocksAndProgrammingElements() {
    // GIVEN
    final PageAnalysis analysis =
        analyze("== T ==\n* {{#if:a|b}} {{{1}}} __TOC__\n[[Fichier:X.jpg|thumb]]");

    // WHEN
    final List<String> spans = describeSpans(analysis);

    // THEN
    Assertions.assertThat(spans)
        .containsExactly(
            "TITLE:7",
            "-:1",
            "LIST_ITEM:1",
            "-:1",
            "PROGRAMMING:11",
            "-:1",
            "PROGRAMMING:7",
            "-:1",
            "PROGRAMMING:7",
            "-:1",
            "IMAGE:23");
  }

  @DisplayName("computeStyleSpans colors the prefix and the colon of definition lists")
  @Test
  void colorDefinitionList() {
    // GIVEN
    final PageAnalysis analysis = analyze("; a : b");

    // WHEN
    final List<String> spans = describeSpans(analysis);

    // THEN
    Assertions.assertThat(spans).containsExactly("LIST_ITEM:1", "-:3", "LIST_ITEM:1", "-:2");
  }

  @DisplayName("computeStyleSpans colors table syntax but not cell contents")
  @Test
  void colorTableSyntax() {
    // GIVEN
    final PageAnalysis analysis = analyze("{| class=x\n|+ Cap\n! H\n|-\n| a || style=y | b\n|}");

    // WHEN
    final List<String> spans = describeSpans(analysis);

    // THEN
    Assertions.assertThat(spans)
        .containsExactly(
            "TABLE:10",
            "-:1",
            "TABLE:2",
            "-:5",
            "TABLE:1",
            "-:3",
            "TABLE:2",
            "-:1",
            "TABLE:1",
            "-:3",
            "TABLE:12",
            "-:3",
            "TABLE:2");
  }

  @DisplayName("computeStyleSpans colors file names in galleries")
  @Test
  void colorGalleryImages() {
    // GIVEN
    final PageAnalysis analysis = analyze("<gallery>\nFile:A.jpg|cap\n</gallery>");

    // WHEN
    final List<String> spans = describeSpans(analysis);

    // THEN
    Assertions.assertThat(spans).containsExactly("TAG:9", "-:1", "IMAGE:10", "-:5", "TAG:10");
  }

  private static PageAnalysis analyze(final String text) {
    final ExtensionTagRepository extensionTagRepository = new ExtensionTagRepository();
    Stream.of("ref", "references", "gallery")
        .forEach(name -> extensionTagRepository.addExtensionTag(WIKI, new ExtensionTag(name)));
    final MagicWordRepository magicWordRepository = new MagicWordRepository();
    magicWordRepository.addMagicWord(
        WIKI, new MagicWord("if", List.of("si", "if"), false, false, true, false));
    magicWordRepository.addMagicWord(
        WIKI, new MagicWord("toc", List.of("__TOC__"), false, true, false, false));
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.FILE.id, "File", "Fichier", List.of(), CaseType.FIRST_LETTER));
    return new PageAnalysisFactory(
            extensionTagRepository,
            new InterwikiRepository(),
            new MagicLinkRepository(),
            magicWordRepository,
            namespaceRepository,
            new ProtocolRepository())
        .analysis(WIKI, "Title", text);
  }

  private List<String> describeSpans(final PageAnalysis analysis) {
    final Map<String, String> styleNames =
        Stream.of(
                Map.entry(PageAnalysisStylePropertiesInitializer.IMAGE, "IMAGE"),
                Map.entry(PageAnalysisStylePropertiesInitializer.INTERNAL_LINK, "INTERNAL_LINK"),
                Map.entry(PageAnalysisStylePropertiesInitializer.LIST_ITEM, "LIST_ITEM"),
                Map.entry(PageAnalysisStylePropertiesInitializer.PROGRAMMING, "PROGRAMMING"),
                Map.entry(PageAnalysisStylePropertiesInitializer.REFERENCE, "REFERENCE"),
                Map.entry(PageAnalysisStylePropertiesInitializer.TABLE, "TABLE"),
                Map.entry(PageAnalysisStylePropertiesInitializer.TAG, "TAG"),
                Map.entry(PageAnalysisStylePropertiesInitializer.TEMPLATE, "TEMPLATE"),
                Map.entry(PageAnalysisStylePropertiesInitializer.TITLE, "TITLE"))
            .collect(
                Collectors.toMap(
                    entry -> styleRegistry.getStyle(entry.getKey()),
                    Map.Entry::getValue,
                    (first, second) -> first + "/" + second));
    final List<String> spans = new ArrayList<>();
    for (final StyleSpan<String> span : colorizer.computeStyleSpans(analysis)) {
      spans.add(styleNames.getOrDefault(span.getStyle(), NO_STYLE) + ":" + span.getLength());
    }
    return spans;
  }
}
