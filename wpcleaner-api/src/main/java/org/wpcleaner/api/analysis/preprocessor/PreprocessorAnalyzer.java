package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.include.IncludeAreaElement;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagAttributeAnalyzer;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.TagType;
import org.wpcleaner.api.analysis.tag.TagTypeRepository;
import org.wpcleaner.api.analysis.tag.WikiTagType;

final class PreprocessorAnalyzer {

  private static final String SELF_CLOSING_END =
      new String(new char[] {TagElement.TOKEN_SLASH, TagElement.TOKEN_END});
  private static final Map<String, IncludeAreaType> AREA_TYPES_BY_TAG_NAME =
      Map.of(
          WikiTagType.INCLUDEONLY.getNormalizedName(), IncludeAreaType.INCLUDE_ONLY,
          WikiTagType.NOINCLUDE.getNormalizedName(), IncludeAreaType.NO_INCLUDE);
  private static final Set<String> PREPROCESSOR_TAG_NAMES =
      Set.of(
          WikiTagType.INCLUDEONLY.getNormalizedName(),
          WikiTagType.NOINCLUDE.getNormalizedName(),
          WikiTagType.ONLYINCLUDE.getNormalizedName());
  private static final String ONLY_INCLUDE_START_TAG =
      "<" + WikiTagType.ONLYINCLUDE.getNormalizedName() + ">";
  private static final String ONLY_INCLUDE_END_TAG =
      "</" + WikiTagType.ONLYINCLUDE.getNormalizedName() + ">";

  private final String text;
  private final Set<String> extensionTagNames;
  private final TextBrowser textBrowser;
  private final TagAttributeAnalyzer attributeAnalyzer;
  private final Map<String, Pattern> endTagPatterns;
  private final List<CommentElement> comments;
  private final List<TagElement> tags;
  private final List<CompleteTagElement> completeTags;
  private final List<CompleteTagElement> nonWikiTags;
  private final List<IncludeAreaElement> includeAreas;
  private final CurlyBracketElementFactory curlyBracketElementFactory;
  private final TitleElementFactory titleElementFactory;

  PreprocessorAnalyzer(
      final String text,
      final Set<String> extensionTagNames,
      final CurlyBracketElementFactory curlyBracketElementFactory,
      final TitleElementFactory titleElementFactory) {
    this.text = text;
    this.extensionTagNames = extensionTagNames;
    this.textBrowser = new TextBrowser(text);
    this.attributeAnalyzer = new TagAttributeAnalyzer(text);
    this.endTagPatterns = new HashMap<>();
    this.comments = new ArrayList<>();
    this.tags = new ArrayList<>();
    this.completeTags = new ArrayList<>();
    this.nonWikiTags = new ArrayList<>();
    this.includeAreas = new ArrayList<>();
    this.curlyBracketElementFactory = curlyBracketElementFactory;
    this.titleElementFactory = titleElementFactory;
  }

  void analyze() {
    analyze(0, text.length());
    analyzeOnlyIncludeAreas();
    comments.sort(Element.COMPARATOR);
    tags.sort(Element.COMPARATOR);
    completeTags.sort(Element.COMPARATOR);
    includeAreas.sort(Element.COMPARATOR);
  }

  List<CommentElement> getComments() {
    return comments;
  }

  List<TagElement> getTags() {
    return tags;
  }

  List<CompleteTagElement> getCompleteTags() {
    return completeTags;
  }

  List<IncludeAreaElement> getIncludeAreas() {
    return includeAreas;
  }

  List<Element> getExclusions() {
    final List<Element> exclusions = new ArrayList<>(comments);
    exclusions.addAll(tags);
    exclusions.addAll(nonWikiTags);
    return exclusions;
  }

  private void analyze(final int begin, final int end) {
    final Set<String> namesWithoutEndTag = new HashSet<>();
    final BracketStack bracketStack =
        new BracketStack(
            text,
            end,
            curlyBrackets -> curlyBracketElementFactory.persist(curlyBrackets, comments),
            titleLine -> titleElementFactory.persist(titleLine, comments));
    int index = begin;
    while (index < end) {
      if (text.charAt(index) == TagElement.TOKEN_START) {
        index = analyzeTagStart(index, end, namesWithoutEndTag);
      } else {
        index = bracketStack.analyze(index);
      }
    }
    bracketStack.finish();
  }

  private int analyzeTagStart(
      final int begin, final int end, final Set<String> namesWithoutEndTag) {
    if (text.startsWith(CommentElement.TOKEN_START, begin)) {
      return analyzeComment(begin, end);
    }
    return analyzeExtensionTag(begin, end, namesWithoutEndTag);
  }

  private int analyzeComment(final int begin, final int end) {
    final int tokenEnd =
        text.indexOf(CommentElement.TOKEN_END, begin + CommentElement.TOKEN_START.length());
    final int commentEnd =
        tokenEnd >= 0 && tokenEnd + CommentElement.TOKEN_END.length() <= end
            ? tokenEnd + CommentElement.TOKEN_END.length()
            : end;
    comments.add(new CommentElement(begin, commentEnd));
    return commentEnd;
  }

  private int analyzeExtensionTag(
      final int begin, final int end, final Set<String> namesWithoutEndTag) {
    final int nameBegin = begin + 1;
    final int nameEnd = findNameEnd(nameBegin, end);
    final String name = text.substring(nameBegin, nameEnd);
    final String normalizedName = name.toLowerCase(Locale.ROOT);
    if (!isPreprocessorTag(normalizedName) || !isAfterName(nameEnd, end)) {
      return nameBegin;
    }
    final int tokenEnd = text.indexOf(TagElement.TOKEN_END, nameEnd);
    if (tokenEnd < 0 || tokenEnd >= end) {
      return nameBegin;
    }
    final int tagEnd = tokenEnd + 1;
    final TextBrowser.Cursor attributesCursor = textBrowser.cursor(nameEnd);
    attributesCursor.moveAfterWhitespace();
    final List<TagAttribute> attributes = attributeAnalyzer.extractAttributes(attributesCursor);
    if (text.charAt(tokenEnd - 1) == TagElement.TOKEN_SLASH) {
      tags.add(new TagElement(begin, tagEnd, name, TagKind.SELF_CLOSING, attributes));
      return tagEnd;
    }
    final TagElement startTag = new TagElement(begin, tagEnd, name, TagKind.START, attributes);
    tags.add(startTag);
    final Optional<TagElement> endTag =
        namesWithoutEndTag.contains(normalizedName)
            ? Optional.empty()
            : findEndTag(normalizedName, tagEnd, end);
    if (endTag.isEmpty()) {
      namesWithoutEndTag.add(normalizedName);
      addIncludeArea(normalizedName, begin, end);
      return tagEnd;
    }
    addIncludeArea(normalizedName, begin, endTag.get().end());
    tags.add(endTag.get());
    final CompleteTagElement completeTag = new CompleteTagElement(startTag, endTag.get());
    completeTags.add(completeTag);
    if (hasWikiContent(normalizedName)) {
      final int contentBegin = startTag.end();
      final int contentEnd = endTag.get().begin();
      analyze(contentBegin, contentEnd);
    } else {
      nonWikiTags.add(completeTag);
    }
    return endTag.get().end();
  }

  private boolean isPreprocessorTag(final String normalizedName) {
    return extensionTagNames.contains(normalizedName)
        || PREPROCESSOR_TAG_NAMES.contains(normalizedName);
  }

  private void addIncludeArea(final String normalizedName, final int begin, final int end) {
    final IncludeAreaType type = AREA_TYPES_BY_TAG_NAME.get(normalizedName);
    if (type != null) {
      includeAreas.add(new IncludeAreaElement(begin, end, type));
    }
  }

  private void analyzeOnlyIncludeAreas() {
    if (!text.contains(ONLY_INCLUDE_END_TAG)) {
      return;
    }
    int begin = text.indexOf(ONLY_INCLUDE_START_TAG);
    while (begin >= 0) {
      final int endTagBegin =
          text.indexOf(ONLY_INCLUDE_END_TAG, begin + ONLY_INCLUDE_START_TAG.length());
      final int end = endTagBegin < 0 ? text.length() : endTagBegin + ONLY_INCLUDE_END_TAG.length();
      includeAreas.add(new IncludeAreaElement(begin, end, IncludeAreaType.ONLY_INCLUDE));
      begin = text.indexOf(ONLY_INCLUDE_START_TAG, end);
    }
  }

  private int findNameEnd(final int nameBegin, final int end) {
    int nameEnd = nameBegin;
    while (nameEnd < end
        && !Character.isWhitespace(text.charAt(nameEnd))
        && text.charAt(nameEnd) != TagElement.TOKEN_SLASH
        && text.charAt(nameEnd) != TagElement.TOKEN_END) {
      nameEnd++;
    }
    return nameEnd;
  }

  private boolean isAfterName(final int index, final int end) {
    if (index >= end) {
      return false;
    }
    return Character.isWhitespace(text.charAt(index))
        || text.charAt(index) == TagElement.TOKEN_END
        || (index + SELF_CLOSING_END.length() <= end && text.startsWith(SELF_CLOSING_END, index));
  }

  private Optional<TagElement> findEndTag(
      final String normalizedName, final int begin, final int end) {
    final Matcher matcher =
        endTagPatterns
            .computeIfAbsent(
                normalizedName,
                name ->
                    Pattern.compile(
                        "</(" + Pattern.quote(name) + ")\\s*>", Pattern.CASE_INSENSITIVE))
            .matcher(text);
    matcher.region(begin, end);
    if (!matcher.find()) {
      return Optional.empty();
    }
    return Optional.of(
        new TagElement(matcher.start(), matcher.end(), matcher.group(1), TagKind.END, List.of()));
  }

  private static boolean hasWikiContent(final String normalizedName) {
    return TagTypeRepository.findTagType(normalizedName).map(TagType::hasWikiContent).orElse(false);
  }
}
