package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;

record BracketedArticleLink(ArticleUrl url, @Nullable String displayedText) {

  private static final char PIPE = '|';
  private static final char CLOSE = ']';
  private static final char SLASH = '/';

  static Optional<BracketedArticleLink> of(
      final PageAnalysis analysis, final ExternalLinkElement link) {
    final String text = analysis.getText();
    final int pipe = link.target().indexOf(PIPE);
    final String url = pipe < 0 ? link.target() : link.target().substring(0, pipe);
    final int contentEnd = text.charAt(link.end() - 1) == CLOSE ? link.end() - 1 : link.end();
    final String content = text.substring(link.begin() + 1, contentEnd);
    final String displayedText =
        pipe < 0 ? link.text() : content.substring(content.indexOf(PIPE) + 1);
    return ArticleUrl.of(analysis.getWiki(), url)
        .filter(articleUrl -> articleUrl.attributes().isEmpty())
        .map(articleUrl -> new BracketedArticleLink(articleUrl, displayedText));
  }

  String internalLink() {
    return InternalLinkBuilder.to(url.title())
        .withAnchor(url.fragment())
        .withColon(url.title().charAt(0) == SLASH)
        .withText(displayedText == null || displayedText.isEmpty() ? null : displayedText)
        .build();
  }
}
