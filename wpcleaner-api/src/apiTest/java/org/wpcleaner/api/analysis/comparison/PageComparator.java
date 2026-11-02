package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.gallery.GalleryImage;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.api.parse.Category;
import org.wpcleaner.api.api.parse.Parse;
import org.wpcleaner.api.api.parse.Section;
import org.wpcleaner.api.api.parse.TableOfContents;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

final class PageComparator {

  private static final int SNIPPET_LENGTH = 60;
  private static final String HELP_NAMESPACE_PREFIX = CommonNamespaces.HELP.id + ":";

  private final List<Difference> differences;

  PageComparator() {
    this.differences = new ArrayList<>();
  }

  List<Difference> getDifferences() {
    return differences;
  }

  void comparePreprocessor(
      final PageAnalysis analysis, final ParseTree parseTree, final Set<String> extensionTags) {
    final String text = analysis.getText();
    final List<ParseTreeElement> mediaWikiExtensionTags =
        parseTree.elements(ParseTreeElement.Kind.EXTENSION_TAG);
    compareRanges(
        "template",
        text,
        parseTree.elements(ParseTreeElement.Kind.TEMPLATE),
        outside(
            mediaWikiExtensionTags,
            Stream.concat(analysis.getTemplates().stream(), analysis.getFunctions().stream())));
    compareRanges(
        "template parameter",
        text,
        parseTree.elements(ParseTreeElement.Kind.TEMPLATE_ARGUMENT),
        outside(mediaWikiExtensionTags, analysis.getParameters().stream()));
    compareRanges(
        "comment",
        text,
        parseTree.elements(ParseTreeElement.Kind.COMMENT).stream()
            .map(comment -> trimWhitespace(text, comment))
            .toList(),
        outside(mediaWikiExtensionTags, analysis.getComments().stream()));
    compareRanges(
        "extension tag",
        text,
        mediaWikiExtensionTags,
        outside(
            mediaWikiExtensionTags,
            Stream.concat(
                analysis.getCompleteTags().stream()
                    .filter(tag -> extensionTags.contains(tag.startTag().normalizedName())),
                analysis.getTags().stream()
                    .filter(tag -> tag.kind() == TagKind.SELF_CLOSING)
                    .filter(tag -> extensionTags.contains(tag.normalizedName())))));
    compareSets(
        "title",
        parseTree.elements(ParseTreeElement.Kind.HEADING).stream()
            .map(heading -> describeTitle(text, heading.begin(), heading.level()))
            .collect(Collectors.toSet()),
        outside(mediaWikiExtensionTags, analysis.getTitles().stream()).stream()
            .map(title -> describeTitle(text, title.begin(), ((TitleElement) title).level()))
            .collect(Collectors.toSet()));
  }

  private static List<Element> outside(
      final List<ParseTreeElement> extensionTags, final Stream<? extends Element> elements) {
    return elements
        .filter(
            element ->
                extensionTags.stream()
                    .noneMatch(tag -> tag.begin() < element.begin() && element.end() <= tag.end()))
        .map(Element.class::cast)
        .toList();
  }

  private static ParseTreeElement trimWhitespace(
      final String text, final ParseTreeElement element) {
    int begin = element.begin();
    int end = element.end();
    while (begin < end && Character.isWhitespace(text.charAt(begin))) {
      begin++;
    }
    while (end > begin && Character.isWhitespace(text.charAt(end - 1))) {
      end--;
    }
    return new ParseTreeElement(element.kind(), begin, end, element.level());
  }

  void compareWikiElements(
      final PageAnalysis analysis, final Parse parse, final UnaryOperator<Set<String>> usedImages) {
    final Set<String> wpCleanerCategories =
        analysis.getCategories().stream()
            .map(CategoryElement::title)
            .filter(Objects::nonNull)
            .map(PageTitle::text)
            .collect(Collectors.toSet());
    final Set<String> wpCleanerLinks =
        analysis.getInternalLinks().stream()
            .map(InternalLinkElement::title)
            .filter(Objects::nonNull)
            .filter(title -> title.namespace() >= 0)
            .map(title -> describeTitle(title.namespace(), title.text()))
            .collect(Collectors.toSet());
    compareSets(
        "internal link",
        parse.links().stream()
            .map(
                link ->
                    describeTitle(
                        link.namespace(),
                        link.namespace() == 0 ? link.title() : removePrefix(link.title())))
            .filter(
                link -> !link.startsWith(HELP_NAMESPACE_PREFIX) || wpCleanerLinks.contains(link))
            .collect(Collectors.toSet()),
        wpCleanerLinks);
    compareSets(
        "category",
        parse.categories().stream()
            .filter(
                category ->
                    !Boolean.TRUE.equals(category.hidden())
                        || wpCleanerCategories.contains(normalize(category.category())))
            .map(Category::category)
            .map(PageComparator::normalize)
            .collect(Collectors.toSet()),
        wpCleanerCategories);
    final Set<String> wpCleanerImages =
        Stream.concat(
                analysis.getImages().stream().map(image -> image.title().text()),
                analysis.getGalleries().stream()
                    .flatMap(gallery -> gallery.images().stream())
                    .map(GalleryImage::title)
                    .filter(Objects::nonNull)
                    .map(PageTitle::text))
            .collect(Collectors.toSet());
    final Set<String> mediaWikiImages =
        parse.images().stream().map(PageComparator::normalize).collect(Collectors.toSet());
    compareSets(
        "image",
        withoutRedirectTargets(mediaWikiImages, wpCleanerImages, usedImages),
        wpCleanerImages);
    compareSets(
        "external link",
        parse.externalLinks().stream().map(PageComparator::decodeUrl).collect(Collectors.toSet()),
        analysis.getExternalLinks().stream()
            .map(link -> decodeUrl(link.target()))
            .collect(Collectors.toSet()));
    compareSets(
        "interwiki link",
        parse.interwikiLinks().stream()
            .map(link -> link.prefix() + ":" + normalize(removePrefix(link.title())))
            .collect(Collectors.toSet()),
        analysis.getInterwikiLinks().stream()
            .map(link -> link.prefix() + ":" + normalize(link.target()))
            .collect(Collectors.toSet()));
    compareSets(
        "language link",
        parse.languageLinks().stream()
            .map(link -> link.language() + ":" + normalize(link.title()))
            .collect(Collectors.toSet()),
        analysis.getLanguageLinks().stream()
            .map(link -> link.language() + ":" + normalize(link.target()))
            .collect(Collectors.toSet()));
    final TableOfContents tableOfContents = parse.tableOfContents();
    final List<Section> sections = tableOfContents == null ? List.of() : tableOfContents.sections();
    final String text = analysis.getText();
    compareSets(
        "section",
        sections.stream()
            .filter(section -> section.codepointOffset() != null)
            .map(
                section ->
                    describeTitle(
                        text,
                        text.offsetByCodePoints(0, section.codepointOffset()),
                        section.headingLevel()))
            .collect(Collectors.toSet()),
        analysis.getTitles().stream()
            .map(title -> describeTitle(text, title.begin(), title.level()))
            .collect(Collectors.toSet()));
  }

  private void compareRanges(
      final String type,
      final String text,
      final List<ParseTreeElement> mediaWikiElements,
      final List<? extends Element> wpCleanerElements) {
    compareSets(
        type,
        mediaWikiElements.stream()
            .map(element -> describeRange(text, element.begin(), element.end()))
            .collect(Collectors.toSet()),
        wpCleanerElements.stream()
            .map(element -> describeRange(text, element.begin(), element.end()))
            .collect(Collectors.toSet()));
  }

  private static Set<String> withoutRedirectTargets(
      final Set<String> mediaWikiImages,
      final Set<String> wpCleanerImages,
      final UnaryOperator<Set<String>> usedImages) {
    if (wpCleanerImages.containsAll(mediaWikiImages)) {
      return mediaWikiImages;
    }
    final Set<String> redirectTargets =
        usedImages.apply(wpCleanerImages).stream()
            .map(PageComparator::normalize)
            .filter(image -> !wpCleanerImages.contains(image))
            .collect(Collectors.toSet());
    return mediaWikiImages.stream()
        .filter(image -> !redirectTargets.contains(image))
        .collect(Collectors.toSet());
  }

  private void compareSets(
      final String type, final Set<String> mediaWikiValues, final Set<String> wpCleanerValues) {
    final SortedSet<String> onlyMediaWiki = new TreeSet<>(mediaWikiValues);
    onlyMediaWiki.removeAll(wpCleanerValues);
    final SortedSet<String> onlyWpCleaner = new TreeSet<>(wpCleanerValues);
    onlyWpCleaner.removeAll(mediaWikiValues);
    onlyMediaWiki.forEach(
        value -> differences.add(new Difference(type, Difference.Side.ONLY_MEDIAWIKI, value)));
    onlyWpCleaner.forEach(
        value -> differences.add(new Difference(type, Difference.Side.ONLY_WPCLEANER, value)));
  }

  private static String describeRange(final String text, final int begin, final int end) {
    return "%07d-%07d %s".formatted(begin, end, snippet(text, begin, end));
  }

  private static String describeTitle(final String text, final int begin, final int level) {
    final int lineEnd = text.indexOf('\n', begin);
    return "%07d level %d %s"
        .formatted(begin, level, snippet(text, begin, lineEnd < 0 ? text.length() : lineEnd));
  }

  private static String describeTitle(final int namespace, final String title) {
    return namespace + ":" + title;
  }

  private static String snippet(final String text, final int begin, final int end) {
    final int snippetEnd = Math.min(end, Math.min(text.length(), begin + SNIPPET_LENGTH));
    final String snippet = text.substring(begin, Math.max(begin, snippetEnd));
    return "`" + snippet.replace("\n", "⏎").replace("`", "'") + (snippetEnd < end ? "…`" : "`");
  }

  private static String removePrefix(final String title) {
    final int colon = title.indexOf(':');
    return colon < 0 ? title : title.substring(colon + 1);
  }

  private static String decodeUrl(final String url) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final StringBuilder decoded = new StringBuilder();
    int index = 0;
    while (index < url.length()) {
      if (url.charAt(index) == '%'
          && index + 2 < url.length()
          && HexFormat.isHexDigit(url.charAt(index + 1))
          && HexFormat.isHexDigit(url.charAt(index + 2))) {
        bytes.write(HexFormat.fromHexDigits(url, index + 1, index + 3));
        index += 3;
      } else {
        decoded.append(bytes.toString(StandardCharsets.UTF_8));
        bytes.reset();
        decoded.append(url.charAt(index));
        index++;
      }
    }
    return decoded.append(bytes.toString(StandardCharsets.UTF_8)).toString();
  }

  private static String normalize(final String value) {
    return value.replace('_', ' ');
  }
}
