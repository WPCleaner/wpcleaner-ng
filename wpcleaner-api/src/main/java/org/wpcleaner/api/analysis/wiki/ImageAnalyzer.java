package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;

final class ImageAnalyzer {

  private static final char NAMESPACE_SEPARATOR = ':';
  private static final int TWO = 2;

  private final String text;
  private final TextBrowser textBrowser;
  @Nullable private final Namespace fileNamespace;
  private final ImageParameterFactory parameterFactory;
  private final Map<Integer, Integer> curlyBracketEnds;
  private final List<ImageElement> images;

  ImageAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final List<Namespace> namespaces,
      final ImageParameterFactory parameterFactory,
      final List<? extends Element> curlyBracketElements) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.fileNamespace = Namespace.findNamespace(namespaces, CommonNamespaces.FILE.id).orElse(null);
    this.parameterFactory = parameterFactory;
    this.curlyBracketEnds = new HashMap<>();
    curlyBracketElements.forEach(
        element -> curlyBracketEnds.merge(element.begin(), element.end(), Math::max));
    this.images = new ArrayList<>();
  }

  List<ImageElement> getImages() {
    return images;
  }

  boolean analyze(final int begin, final String target, final int separatorIndex) {
    final int column = target.indexOf(NAMESPACE_SEPARATOR);
    if (column <= 0 || fileNamespace == null) {
      return false;
    }
    final String namespace = target.substring(0, column).trim();
    if (!fileNamespace.isPossibleName(namespace)) {
      return false;
    }
    final String name = target.substring(column + 1).trim();
    final Optional<ImageElement> image =
        new ParameterScan(separatorIndex).scan(begin, namespace, name);
    image.ifPresent(images::add);
    return image.isPresent();
  }

  private final class ParameterScan {

    private final int separatorIndex;
    private final List<ImageParameter> parameters;
    private TextBrowser.Cursor cursor;
    private int parameterBegin;
    private int linkDepth;

    private ParameterScan(final int separatorIndex) {
      this.separatorIndex = separatorIndex;
      this.parameters = new ArrayList<>();
      this.cursor = textBrowser.cursor(separatorIndex);
      this.parameterBegin = separatorIndex + 1;
      this.linkDepth = 0;
    }

    private Optional<ImageElement> scan(
        final int begin, final String namespace, final String name) {
      while (cursor.getIndex() < text.length()) {
        final int index = cursor.getIndex();
        final Integer curlyBracketEnd = curlyBracketEnds.get(index);
        if (curlyBracketEnd == null) {
          final OptionalInt end = analyzeCharacter(index);
          if (end.isPresent()) {
            return Optional.of(
                new ImageElement(begin, end.getAsInt(), namespace, name, parameters));
          }
        } else {
          cursor = textBrowser.cursor(curlyBracketEnd);
        }
      }
      return Optional.empty();
    }

    private OptionalInt analyzeCharacter(final int index) {
      final char currentChar = text.charAt(index);
      cursor.moveNext();
      final boolean doubled = isRepeated(currentChar);
      if (currentChar == SquareBracketAnalyzer.OPEN && doubled) {
        linkDepth++;
        cursor.moveNext();
      } else if (currentChar == SquareBracketAnalyzer.CLOSE && doubled) {
        if (linkDepth == 0) {
          final int parameterEnd = computeLastParameterEnd(index);
          addParameter(parameterEnd);
          return OptionalInt.of(parameterEnd + TWO);
        }
        linkDepth--;
        cursor.moveNext();
      } else if (currentChar == SquareBracketAnalyzer.PIPE && linkDepth == 0) {
        addParameter(index);
        parameterBegin = index + 1;
      }
      return OptionalInt.empty();
    }

    private void addParameter(final int parameterEnd) {
      if (parameterEnd > separatorIndex) {
        parameters.add(parameterFactory.create(text, parameterBegin, parameterEnd));
      }
    }

    private int computeLastParameterEnd(final int closingIndex) {
      final boolean thirdClosingBracket =
          closingIndex + TWO < text.length()
              && text.charAt(closingIndex + TWO) == SquareBracketAnalyzer.CLOSE;
      final boolean openingBracket =
          text.substring(separatorIndex, closingIndex).indexOf(SquareBracketAnalyzer.OPEN) >= 0;
      return thirdClosingBracket && openingBracket ? closingIndex + 1 : closingIndex;
    }

    private boolean isRepeated(final char currentChar) {
      return cursor.samePart()
          && cursor.getIndex() < text.length()
          && text.charAt(cursor.getIndex()) == currentChar;
    }
  }
}
