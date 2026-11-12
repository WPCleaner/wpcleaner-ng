package org.wpcleaner.api.analysis.timeline;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.WikiTagType;

final class TimelineAnalyzer {

  private static final char QUOTE = '"';
  private static final char OPEN = '[';
  private static final char PIPE = '|';
  private static final String EXTERNAL_LINK_END = "]";
  private static final char NEW_LINE = '\n';
  private static final char COMMENT = '#';
  private static final String BLOCK_COMMENT_BEGIN = "#>";
  private static final String BLOCK_COMMENT_END = "<#";
  private static final String INTERNAL_LINK_BEGIN = "[[";
  private static final String INTERNAL_LINK_END = "]]";
  private static final String LINK_ATTRIBUTE = "link:";

  private final String text;

  TimelineAnalyzer(final String text) {
    this.text = text;
  }

  List<TimelineLinkElement> analyze(final List<CompleteTagElement> completeTags) {
    final List<TimelineLinkElement> links = new ArrayList<>();
    completeTags.stream()
        .filter(
            tag -> WikiTagType.TIMELINE.getNormalizedName().equals(tag.startTag().normalizedName()))
        .forEach(tag -> new ContentScan(tag.startTag().end(), tag.endTag().begin(), links).scan());
    return links;
  }

  private final class ContentScan {

    private final int contentBegin;
    private final int contentEnd;
    private final List<TimelineLinkElement> links;
    private int index;
    private boolean quoted;

    private ContentScan(
        final int contentBegin, final int contentEnd, final List<TimelineLinkElement> links) {
      this.contentBegin = contentBegin;
      this.contentEnd = contentEnd;
      this.links = links;
      this.index = contentBegin;
    }

    private void scan() {
      while (index < contentEnd) {
        index = analyzeCharacter(text.charAt(index));
      }
    }

    private int analyzeCharacter(final char current) {
      if (current == NEW_LINE || current == QUOTE) {
        quoted = current == QUOTE && !quoted;
        return index + 1;
      }
      if (!quoted) {
        if (text.startsWith(BLOCK_COMMENT_BEGIN, index)) {
          return endOf(BLOCK_COMMENT_END, index + BLOCK_COMMENT_BEGIN.length());
        }
        if (current == COMMENT) {
          return lineEnd(index);
        }
        if (isLinkAttribute()) {
          return analyzeLinkAttribute();
        }
      }
      if (text.startsWith(INTERNAL_LINK_BEGIN, index)) {
        return analyzeEmbeddedLink(INTERNAL_LINK_END, TimelineLinkType.INTERNAL_LINK);
      }
      if (current == OPEN) {
        return analyzeEmbeddedLink(EXTERNAL_LINK_END, TimelineLinkType.EXTERNAL_LINK);
      }
      return index + 1;
    }

    private int analyzeEmbeddedLink(final String end, final TimelineLinkType type) {
      final int targetBegin = index + (type == TimelineLinkType.INTERNAL_LINK ? 2 : 1);
      final int linkEnd = text.indexOf(end, targetBegin);
      if (linkEnd < 0 || linkEnd >= lineEnd(targetBegin)) {
        return targetBegin;
      }
      final int pipe = text.indexOf(PIPE, targetBegin);
      final boolean withText = pipe >= 0 && pipe < linkEnd;
      final String target = text.substring(targetBegin, withText ? pipe : linkEnd);
      @Nullable final String linkText = withText ? text.substring(pipe + 1, linkEnd) : null;
      links.add(new TimelineLinkElement(index, linkEnd + end.length(), type, target, linkText));
      return linkEnd + end.length();
    }

    private boolean isLinkAttribute() {
      return text.regionMatches(true, index, LINK_ATTRIBUTE, 0, LINK_ATTRIBUTE.length())
          && (index == contentBegin || Character.isWhitespace(text.charAt(index - 1)));
    }

    private int analyzeLinkAttribute() {
      final int targetBegin = index + LINK_ATTRIBUTE.length();
      int targetEnd = targetBegin;
      while (targetEnd < contentEnd
          && !Character.isWhitespace(text.charAt(targetEnd))
          && text.charAt(targetEnd) != COMMENT) {
        targetEnd++;
      }
      if (targetEnd > targetBegin) {
        links.add(
            new TimelineLinkElement(
                index,
                targetEnd,
                TimelineLinkType.LINK_ATTRIBUTE,
                text.substring(targetBegin, targetEnd),
                null));
      }
      return targetEnd;
    }

    private int lineEnd(final int begin) {
      final int newLine = text.indexOf(NEW_LINE, begin);
      return newLine < 0 || newLine >= contentEnd ? contentEnd : newLine;
    }

    private int endOf(final String searched, final int begin) {
      final int found = text.indexOf(searched, begin);
      return found < 0 ? contentEnd : found + searched.length();
    }
  }
}
