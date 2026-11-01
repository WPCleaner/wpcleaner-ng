package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.gallery.GalleryElement;
import org.wpcleaner.api.analysis.gallery.GalleryImage;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

final class GalleryAnalyzer {

  private static final char NEW_LINE = '\n';
  private static final char PERCENT = '%';
  private static final int HEX_LENGTH = 2;
  private static final Pattern INVALID_TITLE_CHARACTERS = Pattern.compile("[<>\\[\\]{}|]");

  private final String text;
  private final TextBrowser textBrowser;
  private final PageTitleNormalizer pageTitleNormalizer;
  private final ImageParameterFactory parameterFactory;
  private final Map<Integer, Integer> curlyBracketEnds;
  private final List<CommentElement> comments;

  GalleryAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final PageTitleNormalizer pageTitleNormalizer,
      final ImageParameterFactory parameterFactory,
      final List<? extends Element> curlyBracketElements,
      final List<CommentElement> comments) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.pageTitleNormalizer = pageTitleNormalizer;
    this.parameterFactory = parameterFactory;
    this.curlyBracketEnds = new HashMap<>();
    curlyBracketElements.forEach(
        element -> curlyBracketEnds.merge(element.begin(), element.end(), Math::max));
    this.comments = comments;
  }

  List<GalleryElement> analyze(final List<CompleteTagElement> completeTags) {
    return completeTags.stream()
        .filter(
            tag -> WikiTagType.GALLERY.getNormalizedName().equals(tag.startTag().normalizedName()))
        .map(this::analyzeGallery)
        .toList();
  }

  private GalleryElement analyzeGallery(final CompleteTagElement tag) {
    final int contentEnd = tag.endTag().begin();
    final List<GalleryImage> images = new ArrayList<>();
    int lineBegin = tag.startTag().end();
    while (lineBegin <= contentEnd) {
      final int newLine = text.indexOf(NEW_LINE, lineBegin);
      final int lineEnd = newLine < 0 || newLine > contentEnd ? contentEnd : newLine;
      analyzeLine(lineBegin, lineEnd).ifPresent(images::add);
      lineBegin = lineEnd + 1;
    }
    return new GalleryElement(tag, images);
  }

  private Optional<GalleryImage> analyzeLine(final int begin, final int end) {
    if (isBlankWithoutComments(begin, end)) {
      return Optional.empty();
    }
    final int separator = text.indexOf(SquareBracketAnalyzer.PIPE, begin);
    final int nameEnd = separator < 0 || separator >= end ? end : separator;
    if (nameEnd == begin) {
      return Optional.empty();
    }
    final String name = text.substring(begin, nameEnd);
    final List<ImageParameter> parameters =
        nameEnd < end ? new ParameterScan(nameEnd, end).scan() : List.of();
    return Optional.of(new GalleryImage(begin, end, name.trim(), createTitle(name), parameters));
  }

  @Nullable
  private PageTitle createTitle(final String name) {
    final String decodedName = name.indexOf(PERCENT) < 0 ? name : decodePercent(name);
    if (INVALID_TITLE_CHARACTERS.matcher(decodedName).find()) {
      return null;
    }
    return pageTitleNormalizer.normalize(decodedName, CommonNamespaces.FILE.id).orElse(null);
  }

  private boolean isBlankWithoutComments(final int begin, final int end) {
    int index = begin;
    for (final CommentElement comment : comments) {
      if (comment.end() > begin && comment.begin() < end) {
        if (!text.substring(index, Math.max(index, comment.begin())).isBlank()) {
          return false;
        }
        index = Math.max(index, comment.end());
      }
    }
    return index >= end || text.substring(index, end).isBlank();
  }

  private static String decodePercent(final String value) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final StringBuilder decoded = new StringBuilder(value.length());
    int index = 0;
    while (index < value.length()) {
      if (value.charAt(index) == PERCENT
          && index + HEX_LENGTH < value.length()
          && HexFormat.isHexDigit(value.charAt(index + 1))
          && HexFormat.isHexDigit(value.charAt(index + HEX_LENGTH))) {
        bytes.write(HexFormat.fromHexDigits(value, index + 1, index + HEX_LENGTH + 1));
        index += HEX_LENGTH + 1;
      } else {
        decoded.append(bytes.toString(StandardCharsets.UTF_8));
        bytes.reset();
        decoded.append(value.charAt(index));
        index++;
      }
    }
    return decoded.append(bytes.toString(StandardCharsets.UTF_8)).toString();
  }

  private final class ParameterScan {

    private final int end;
    private final List<ImageParameter> parameters;
    private TextBrowser.Cursor cursor;
    private int parameterBegin;
    private int linkDepth;

    private ParameterScan(final int separator, final int end) {
      this.end = end;
      this.parameters = new ArrayList<>();
      this.parameterBegin = separator + 1;
      this.cursor = textBrowser.cursor(parameterBegin);
      this.linkDepth = 0;
    }

    private List<ImageParameter> scan() {
      while (cursor.getIndex() < end) {
        final int index = cursor.getIndex();
        final Integer curlyBracketEnd = curlyBracketEnds.get(index);
        if (curlyBracketEnd == null) {
          analyzeCharacter(index);
        } else {
          cursor = textBrowser.cursor(curlyBracketEnd);
        }
      }
      parameters.add(parameterFactory.create(text, parameterBegin, end));
      return parameters;
    }

    private void analyzeCharacter(final int index) {
      final char currentChar = text.charAt(index);
      cursor.moveNext();
      final boolean doubled = isRepeated(currentChar);
      if (currentChar == SquareBracketAnalyzer.OPEN && doubled) {
        linkDepth++;
        cursor.moveNext();
      } else if (currentChar == SquareBracketAnalyzer.CLOSE && doubled && linkDepth > 0) {
        linkDepth--;
        cursor.moveNext();
      } else if (currentChar == SquareBracketAnalyzer.PIPE && linkDepth == 0) {
        parameters.add(parameterFactory.create(text, parameterBegin, index));
        parameterBegin = index + 1;
      }
    }

    private boolean isRepeated(final char currentChar) {
      return cursor.samePart()
          && cursor.getIndex() < text.length()
          && text.charAt(cursor.getIndex()) == currentChar;
    }
  }
}
