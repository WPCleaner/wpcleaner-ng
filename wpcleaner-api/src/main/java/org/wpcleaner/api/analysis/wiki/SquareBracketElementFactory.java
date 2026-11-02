package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;

final class SquareBracketElementFactory {

  private static final String LEADING_COLON = ":";

  @Nullable private final Namespace categoryNamespace;
  private final List<Interwiki> interwikis;
  private final PageTitleNormalizer pageTitleNormalizer;
  private final List<CategoryElement> categories;
  private final List<InternalLinkElement> internalLinks;
  private final List<InterwikiLinkElement> interwikiLinks;
  private final List<LanguageLinkElement> languageLinks;

  SquareBracketElementFactory(final List<Interwiki> interwikis, final List<Namespace> namespaces) {
    this.categoryNamespace =
        Namespace.findNamespace(namespaces, CommonNamespaces.CATEGORY.id).orElse(null);
    this.interwikis = interwikis;
    this.pageTitleNormalizer = new PageTitleNormalizer(namespaces);
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
        persistInterwikiLink(linkParts, beforeColumn, afterColumn);
      }
    } else if (!leadingColon
        && categoryNamespace != null
        && categoryNamespace.isPossibleName(beforeColumn)) {
      categories.add(
          new CategoryElement(
              begin,
              end,
              beforeColumn,
              afterColumn.trim(),
              pageTitleNormalizer.normalize(linkTarget, CommonNamespaces.MAIN.id).orElse(null),
              value));
    } else {
      persistInternalLink(linkParts, linkTarget);
    }
  }

  Element persistRedirectLink(
      final int begin, final int end, final String target, @Nullable final String value) {
    final String targetWithoutLeadingSpaces = target.stripLeading();
    final boolean leadingColon = targetWithoutLeadingSpaces.startsWith(LEADING_COLON);
    final String linkTarget =
        leadingColon ? targetWithoutLeadingSpaces.substring(LEADING_COLON.length()) : target;
    final LinkParts linkParts = new LinkParts(begin, end, value, leadingColon);
    final int column = linkTarget.indexOf(':');
    if (column > 0) {
      final String beforeColumn = linkTarget.substring(0, column).trim();
      if (Interwiki.findInterwiki(interwikis, beforeColumn).isPresent()) {
        return persistInterwikiLink(linkParts, beforeColumn, linkTarget.substring(column + 1));
      }
    }
    return persistInternalLink(linkParts, linkTarget);
  }

  private InterwikiLinkElement persistInterwikiLink(
      final LinkParts linkParts, final String prefix, final String target) {
    final AnchoredTarget anchoredTarget = AnchoredTarget.split(target);
    final InterwikiLinkElement interwikiLink =
        new InterwikiLinkElement(
            linkParts.begin(),
            linkParts.end(),
            prefix,
            anchoredTarget.target(),
            anchoredTarget.anchor(),
            linkParts.text(),
            linkParts.leadingColon());
    interwikiLinks.add(interwikiLink);
    return interwikiLink;
  }

  private InternalLinkElement persistInternalLink(final LinkParts linkParts, final String target) {
    final AnchoredTarget anchoredTarget = AnchoredTarget.split(target);
    final InternalLinkElement internalLink =
        new InternalLinkElement(
            linkParts.begin(),
            linkParts.end(),
            anchoredTarget.target(),
            pageTitleNormalizer.normalize(target, CommonNamespaces.MAIN.id).orElse(null),
            anchoredTarget.anchor(),
            linkParts.text(),
            linkParts.leadingColon());
    internalLinks.add(internalLink);
    return internalLink;
  }

  private record LinkParts(int begin, int end, @Nullable String text, boolean leadingColon) {}

  private record AnchoredTarget(String target, @Nullable String anchor) {

    private static final char ANCHOR = '#';
    private static final Pattern NUMERIC_CHARACTER_REFERENCE =
        Pattern.compile("&#(\\d+|[xX][0-9a-fA-F]+);");

    static AnchoredTarget split(final String fullTarget) {
      final int anchorIndex = findAnchor(fullTarget);
      if (anchorIndex < 0) {
        return new AnchoredTarget(fullTarget.trim(), null);
      }
      return new AnchoredTarget(
          fullTarget.substring(0, anchorIndex).trim(),
          fullTarget.substring(anchorIndex + 1).trim());
    }

    private static int findAnchor(final String fullTarget) {
      final Matcher matcher = NUMERIC_CHARACTER_REFERENCE.matcher(fullTarget);
      int anchorIndex = fullTarget.indexOf(ANCHOR);
      while (anchorIndex > 0
          && matcher.find(anchorIndex - 1)
          && matcher.start() == anchorIndex - 1) {
        anchorIndex = fullTarget.indexOf(ANCHOR, matcher.end());
      }
      return anchorIndex;
    }
  }
}
