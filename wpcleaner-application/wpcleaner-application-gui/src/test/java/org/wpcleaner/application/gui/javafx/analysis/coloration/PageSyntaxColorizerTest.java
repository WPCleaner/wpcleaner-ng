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
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
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
    final PageAnalysisFactory factory = new PageAnalysisFactory(new NamespaceRepository());
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
}
