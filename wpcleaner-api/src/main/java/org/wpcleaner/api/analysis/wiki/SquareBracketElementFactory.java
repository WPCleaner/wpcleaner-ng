package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;

@SuppressWarnings("PMD.DataClass")
final class SquareBracketElementFactory {

  private static final String LEADING_COLON = ":";

  @Nullable private final Namespace categoryNamespace;
  private final List<Interwiki> interwikis;
  private final List<CategoryElement> categories;
  private final List<InternalLinkElement> internalLinks;
  private final List<InterwikiLinkElement> interwikiLinks;
  private final List<LanguageLinkElement> languageLinks;

  SquareBracketElementFactory(final List<Interwiki> interwikis, final List<Namespace> namespaces) {
    this.categoryNamespace =
        Namespace.findNamespace(namespaces, CommonNamespaces.CATEGORY.id).orElse(null);
    this.interwikis = interwikis;
    this.categories = new ArrayList<>();
    this.internalLinks = new ArrayList<>();
    this.interwikiLinks = new ArrayList<>();
    this.languageLinks = new ArrayList<>();
  }

  List<CategoryElement> getCategories() {
    return categories;
  }

  List<InternalLinkElement> getInternalLinks() {
    return internalLinks;
  }

  List<InterwikiLinkElement> getInterwikiLinks() {
    return interwikiLinks;
  }

  List<LanguageLinkElement> getLanguageLinks() {
    return languageLinks;
  }

  void persist(final int begin, final int end, final String target, @Nullable final String value) {
    final String targetWithoutLeadingSpaces = target.stripLeading();
    final boolean leadingColon = targetWithoutLeadingSpaces.startsWith(LEADING_COLON);
    final String linkTarget =
        leadingColon ? targetWithoutLeadingSpaces.substring(LEADING_COLON.length()) : target;
    final LinkParts linkParts = new LinkParts(begin, end, value, leadingColon);
    final int column = linkTarget.indexOf(':');
    if (column <= 0) {
      persistInternalLink(linkParts, linkTarget);
      return;
    }
    final String beforeColumn = linkTarget.substring(0, column).trim();
    final String afterColumn = linkTarget.substring(column + 1);
    final Optional<Interwiki> interwikiOpt = Interwiki.findInterwiki(interwikis, beforeColumn);
    if (interwikiOpt.isPresent()) {
      if (interwikiOpt.get().language() != null && !leadingColon) {
        languageLinks.add(
            new LanguageLinkElement(begin, end, beforeColumn, afterColumn.trim(), value));
      } else {
        final AnchoredTarget anchoredTarget = AnchoredTarget.split(afterColumn);
        interwikiLinks.add(
            new InterwikiLinkElement(
                begin,
                end,
                beforeColumn,
                anchoredTarget.target(),
                anchoredTarget.anchor(),
                value,
                leadingColon));
      }
    } else if (!leadingColon
        && categoryNamespace != null
        && categoryNamespace.isPossibleName(beforeColumn)) {
      categories.add(new CategoryElement(begin, end, beforeColumn, afterColumn.trim(), value));
    } else {
      persistInternalLink(linkParts, linkTarget);
    }
  }

  private void persistInternalLink(final LinkParts linkParts, final String target) {
    final AnchoredTarget anchoredTarget = AnchoredTarget.split(target);
    internalLinks.add(
        new InternalLinkElement(
            linkParts.begin(),
            linkParts.end(),
            anchoredTarget.target(),
            anchoredTarget.anchor(),
            linkParts.text(),
            linkParts.leadingColon()));
  }

  private record LinkParts(int begin, int end, @Nullable String text, boolean leadingColon) {}

  private record AnchoredTarget(String target, @Nullable String anchor) {

    private static final char ANCHOR = '#';

    static AnchoredTarget split(final String fullTarget) {
      final int anchorIndex = fullTarget.indexOf(ANCHOR);
      if (anchorIndex < 0) {
        return new AnchoredTarget(fullTarget.trim(), null);
      }
      return new AnchoredTarget(
          fullTarget.substring(0, anchorIndex).trim(),
          fullTarget.substring(anchorIndex + 1).trim());
    }
  }
}
