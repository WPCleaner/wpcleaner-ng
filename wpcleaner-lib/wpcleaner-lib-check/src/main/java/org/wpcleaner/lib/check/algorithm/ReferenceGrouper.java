package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.TagType;
import org.wpcleaner.api.analysis.tag.WikiTagType;

record ReferenceGrouper(
    String punctuation, String separator, boolean singleSeparator, Set<TagType> tags) {

  private static final String NON_BREAKING_SPACE = "&nbsp;";
  private static final String SHORT_REFERENCE = "<ref>...</ref>";
  private static final String ELLIPSIS = "...";

  static String shortenReferences(final int count, final String separator) {
    return switch (count) {
      case 1 -> SHORT_REFERENCE;
      case 2 -> SHORT_REFERENCE + separator + SHORT_REFERENCE;
      default -> SHORT_REFERENCE + separator + ELLIPSIS + separator + SHORT_REFERENCE;
    };
  }

  static List<Element> findReferences(
      final PageAnalysis analysis, final Set<PageTitle> referenceTemplates) {
    final String ref = WikiTagType.REF.getNormalizedName();
    final List<Element> references =
        Stream.of(
                analysis.getCompleteTags().stream()
                    .filter(tag -> ref.equals(tag.startTag().normalizedName())),
                analysis.getTags().stream()
                    .filter(tag -> tag.kind() == TagKind.SELF_CLOSING)
                    .filter(tag -> ref.equals(tag.normalizedName())),
                analysis.getTemplates().stream()
                    .filter(template -> referenceTemplates.contains(template.title())))
            .<Element>flatMap(stream -> stream)
            .sorted(Element.COMPARATOR)
            .toList();
    return references.stream()
        .filter(
            reference ->
                references.stream()
                    .noneMatch(other -> !other.equals(reference) && reference.isInside(other)))
        .toList();
  }

  List<List<Element>> group(final PageAnalysis analysis, final List<Element> references) {
    final List<List<Element>> groups = new ArrayList<>();
    List<Element> current = new ArrayList<>();
    for (final Element reference : references) {
      if (!current.isEmpty() && !canGroup(analysis, current.getLast().end(), reference.begin())) {
        groups.add(List.copyOf(current));
        current = new ArrayList<>();
      }
      current.add(reference);
    }
    if (!current.isEmpty()) {
      groups.add(List.copyOf(current));
    }
    return List.copyOf(groups);
  }

  private boolean canGroup(final PageAnalysis analysis, final int from, final int to) {
    final String text = analysis.getText();
    int index = from;
    boolean separatorFound = false;
    while (index < to) {
      if (isSeparatorAt(text, index, separatorFound)) {
        index += separator.length();
        separatorFound = true;
      } else if (text.startsWith(NON_BREAKING_SPACE, index)) {
        index += NON_BREAKING_SPACE.length();
      } else if (Character.isWhitespace(text.charAt(index))
          || punctuation.indexOf(text.charAt(index)) >= 0) {
        index++;
      } else {
        final Optional<Element> tag = findGroupingTag(analysis, index);
        if (tag.isEmpty()) {
          return false;
        }
        index = tag.get().end();
      }
    }
    return true;
  }

  private boolean isSeparatorAt(final String text, final int index, final boolean separatorFound) {
    return !separator.isEmpty()
        && (!singleSeparator || !separatorFound)
        && text.startsWith(separator, index);
  }

  private Optional<Element> findGroupingTag(final PageAnalysis analysis, final int index) {
    return Stream.<Element>concat(
            analysis.getCompleteTags().stream()
                .filter(tag -> tag.begin() == index && isGroupingTag(tag.startTag()))
                .filter(tag -> canGroup(analysis, tag.startTag().end(), tag.endTag().begin())),
            analysis.getTags().stream()
                .filter(tag -> tag.begin() == index && tag.kind() == TagKind.SELF_CLOSING)
                .filter(this::isGroupingTag))
        .findFirst();
  }

  private boolean isGroupingTag(final TagElement tag) {
    return tags.stream().anyMatch(type -> type.getNormalizedName().equals(tag.normalizedName()));
  }
}
