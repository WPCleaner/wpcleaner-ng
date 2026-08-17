package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.fxmisc.richtext.model.StyleSpans;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

class PageSyntaxColorizerTest {

  @DisplayName("computeStyleSpans colors comments and leaves normal text unstyled")
  @Test
  void testComputeStyleSpansWithComment() {
    // Set up style dependencies
    final StylePropertiesRegistry stylePropertiesRegistry =
        new StylePropertiesRegistry(List.of(new PageAnalysisStylePropertiesInitializer()));
    final JavaFxStylePropertiesRegistry javaFxStyleRegistry =
        new JavaFxStylePropertiesRegistry(stylePropertiesRegistry);

    final CommentSyntaxRule commentRule = new CommentSyntaxRule();
    final PageSyntaxColorizer colorizer =
        new PageSyntaxColorizer(List.of(commentRule), javaFxStyleRegistry);

    // Given
    final PageAnalysisFactory factory =
        new PageAnalysisFactory(
            new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());
    final String text = "Hello <!-- world --> !";
    final PageAnalysis analysis = factory.analysis("Title", text);

    // When
    final StyleSpans<String> spans = colorizer.computeStyleSpans(analysis);

    // Then
    Assertions.assertThat(spans.getSpanCount()).isEqualTo(3);

    // First span: "Hello " -> length 6, no style
    Assertions.assertThat(spans.getStyleSpan(0).getLength()).isEqualTo(6);
    Assertions.assertThat(spans.getStyleSpan(0).getStyle()).isEmpty();

    // Second span: "<!-- world -->" -> length 14, COMMENT style
    Assertions.assertThat(spans.getStyleSpan(1).getLength()).isEqualTo(14);
    Assertions.assertThat(spans.getStyleSpan(1).getStyle())
        .isEqualTo(javaFxStyleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.COMMENT));

    // Third span: " !" -> length 2, no style
    Assertions.assertThat(spans.getStyleSpan(2).getLength()).isEqualTo(2);
    Assertions.assertThat(spans.getStyleSpan(2).getStyle()).isEmpty();
  }

  @DisplayName("computeStyleSpans colors language links and leaves normal text unstyled")
  @Test
  void testComputeStyleSpansWithLanguageLink() {
    // Set up style dependencies
    final StylePropertiesRegistry stylePropertiesRegistry =
        new StylePropertiesRegistry(List.of(new PageAnalysisStylePropertiesInitializer()));
    final JavaFxStylePropertiesRegistry javaFxStyleRegistry =
        new JavaFxStylePropertiesRegistry(stylePropertiesRegistry);

    final LanguageLinkSyntaxRule languageLinkRule = new LanguageLinkSyntaxRule();
    final PageSyntaxColorizer colorizer =
        new PageSyntaxColorizer(List.of(languageLinkRule), javaFxStyleRegistry);

    // Given
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));
    final PageAnalysisFactory factory =
        new PageAnalysisFactory(
            interwikiRepository, new NamespaceRepository(), new ProtocolRepository());
    final String text = "Hello [[en:World]] !";
    final PageAnalysis analysis = factory.analysis("Title", text);

    // When
    final StyleSpans<String> spans = colorizer.computeStyleSpans(analysis);

    // Then
    Assertions.assertThat(spans.getSpanCount()).isEqualTo(3);

    // First span: "Hello " -> length 6, no style
    Assertions.assertThat(spans.getStyleSpan(0).getLength()).isEqualTo(6);
    Assertions.assertThat(spans.getStyleSpan(0).getStyle()).isEmpty();

    // Second span: "[[en:World]]" -> length 12, LANGUAGE_LINK style
    Assertions.assertThat(spans.getStyleSpan(1).getLength()).isEqualTo(12);
    Assertions.assertThat(spans.getStyleSpan(1).getStyle())
        .isEqualTo(
            javaFxStyleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.LANGUAGE_LINK));

    // Third span: " !" -> length 2, no style
    Assertions.assertThat(spans.getStyleSpan(2).getLength()).isEqualTo(2);
    Assertions.assertThat(spans.getStyleSpan(2).getStyle()).isEmpty();
  }

  @DisplayName("computeStyleSpans colors interwiki links and leaves normal text unstyled")
  @Test
  void testComputeStyleSpansWithInterwikiLink() {
    // Set up style dependencies
    final StylePropertiesRegistry stylePropertiesRegistry =
        new StylePropertiesRegistry(List.of(new PageAnalysisStylePropertiesInitializer()));
    final JavaFxStylePropertiesRegistry javaFxStyleRegistry =
        new JavaFxStylePropertiesRegistry(stylePropertiesRegistry);

    final InterwikiLinkSyntaxRule interwikiLinkRule = new InterwikiLinkSyntaxRule();
    final PageSyntaxColorizer colorizer =
        new PageSyntaxColorizer(List.of(interwikiLinkRule), javaFxStyleRegistry);

    // Given
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));
    final PageAnalysisFactory factory =
        new PageAnalysisFactory(
            interwikiRepository, new NamespaceRepository(), new ProtocolRepository());
    final String text = "Hello [[fr:World]] !";
    final PageAnalysis analysis = factory.analysis("Title", text);

    // When
    final StyleSpans<String> spans = colorizer.computeStyleSpans(analysis);

    // Then
    Assertions.assertThat(spans.getSpanCount()).isEqualTo(3);

    // First span: "Hello " -> length 6, no style
    Assertions.assertThat(spans.getStyleSpan(0).getLength()).isEqualTo(6);
    Assertions.assertThat(spans.getStyleSpan(0).getStyle()).isEmpty();

    // Second span: "[[fr:World]]" -> length 12, INTERWIKI_LINK style
    Assertions.assertThat(spans.getStyleSpan(1).getLength()).isEqualTo(12);
    Assertions.assertThat(spans.getStyleSpan(1).getStyle())
        .isEqualTo(
            javaFxStyleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.INTERWIKI_LINK));

    // Third span: " !" -> length 2, no style
    Assertions.assertThat(spans.getStyleSpan(2).getLength()).isEqualTo(2);
    Assertions.assertThat(spans.getStyleSpan(2).getStyle()).isEmpty();
  }

  @DisplayName("computeStyleSpans colors external links and leaves normal text unstyled")
  @Test
  void testComputeStyleSpansWithExternalLink() {
    // Set up style dependencies
    final StylePropertiesRegistry stylePropertiesRegistry =
        new StylePropertiesRegistry(List.of(new PageAnalysisStylePropertiesInitializer()));
    final JavaFxStylePropertiesRegistry javaFxStyleRegistry =
        new JavaFxStylePropertiesRegistry(stylePropertiesRegistry);

    final ExternalLinkSyntaxRule externalLinkRule = new ExternalLinkSyntaxRule();
    final PageSyntaxColorizer colorizer =
        new PageSyntaxColorizer(List.of(externalLinkRule), javaFxStyleRegistry);

    // Given
    final ProtocolRepository protocolRepository = new ProtocolRepository();
    protocolRepository.addProtocol(new Protocol("https://"));
    final PageAnalysisFactory factory =
        new PageAnalysisFactory(
            new InterwikiRepository(), new NamespaceRepository(), protocolRepository);
    final String text = "Hello [https://example.com World] !";
    final PageAnalysis analysis = factory.analysis("Title", text);

    // When
    final StyleSpans<String> spans = colorizer.computeStyleSpans(analysis);

    // Then
    Assertions.assertThat(spans.getSpanCount()).isEqualTo(3);

    // First span: "Hello " -> length 6, no style
    Assertions.assertThat(spans.getStyleSpan(0).getLength()).isEqualTo(6);
    Assertions.assertThat(spans.getStyleSpan(0).getStyle()).isEmpty();

    // Second span: "[https://example.com World]" -> length 27, EXTERNAL_LINK style
    Assertions.assertThat(spans.getStyleSpan(1).getLength()).isEqualTo(27);
    Assertions.assertThat(spans.getStyleSpan(1).getStyle())
        .isEqualTo(
            javaFxStyleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.EXTERNAL_LINK));

    // Third span: " !" -> length 2, no style
    Assertions.assertThat(spans.getStyleSpan(2).getLength()).isEqualTo(2);
    Assertions.assertThat(spans.getStyleSpan(2).getStyle()).isEmpty();
  }
}
