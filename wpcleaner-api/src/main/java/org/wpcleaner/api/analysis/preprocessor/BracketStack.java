package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

final class BracketStack {

  static final char CURLY_OPEN = '{';
  static final char CURLY_CLOSE = '}';
  static final char SQUARE_OPEN = '[';
  static final char SQUARE_CLOSE = ']';
  static final char PIPE = '|';
  static final char EQUAL = '=';
  static final char NEW_LINE = '\n';
  private static final int MIN_COUNT = 2;
  private static final int MAX_CURLY_COUNT = 3;
  private static final int MAX_TITLE_COUNT = 6;

  private final String text;
  private final int end;
  private final Consumer<CurlyBrackets> curlyBracketsConsumer;
  private final Consumer<TitleLine> titleLineConsumer;
  private final Deque<Piece> pieces;

  BracketStack(
      final String text,
      final int end,
      final Consumer<CurlyBrackets> curlyBracketsConsumer,
      final Consumer<TitleLine> titleLineConsumer) {
    this.text = text;
    this.end = end;
    this.curlyBracketsConsumer = curlyBracketsConsumer;
    this.titleLineConsumer = titleLineConsumer;
    this.pieces = new ArrayDeque<>();
  }

  int analyze(final int index) {
    final char current = text.charAt(index);
    if (current == CURLY_OPEN || current == SQUARE_OPEN) {
      return open(index, current);
    }
    final Piece top = pieces.peek();
    if (current == EQUAL && isLineStart(index)) {
      return openTitle(index, top);
    }
    if (top == null) {
      return index + 1;
    }
    return analyzeInsidePiece(index, current, top);
  }

  private int analyzeInsidePiece(final int index, final char current, final Piece top) {
    if (current == top.close()) {
      return top.open == EQUAL ? closeTitle(index) : close(index, top);
    }
    if (top.open == CURLY_OPEN && current == PIPE) {
      top.addPart(index);
    } else if (top.open == CURLY_OPEN && current == EQUAL) {
      top.markEqual(index);
    }
    return index + 1;
  }

  void finish() {
    Piece top = pieces.peek();
    while (top != null && top.open == EQUAL) {
      closeTitle(end);
      top = pieces.peek();
    }
  }

  private boolean isLineStart(final int index) {
    return index == 0 || text.charAt(index - 1) == NEW_LINE;
  }

  private int openTitle(final int index, @Nullable final Piece top) {
    final int count = countRun(index, EQUAL, MAX_TITLE_COUNT);
    if (count == 1 && top != null && top.canMarkEqual()) {
      top.markEqual(index);
    } else {
      pieces.push(new Piece(EQUAL, index, count));
    }
    return index + count;
  }

  private int closeTitle(final int index) {
    final Piece piece = pieces.pop();
    titleLineConsumer.accept(new TitleLine(piece.begin, index, piece.count));
    return index;
  }

  private int open(final int index, final char open) {
    final int count = countRun(index, open, end - index);
    if (count >= MIN_COUNT) {
      pieces.push(new Piece(open, index, count));
    }
    return index + count;
  }

  private int close(final int index, final Piece piece) {
    final int count = countRun(index, piece.close(), piece.count);
    final int matchingCount = Math.min(count, piece.maxCount());
    if (matchingCount < MIN_COUNT) {
      return index + count;
    }
    pieces.pop();
    if (piece.open == CURLY_OPEN) {
      curlyBracketsConsumer.accept(
          new CurlyBrackets(
              piece.begin + piece.count - matchingCount,
              index + matchingCount,
              matchingCount,
              piece.parts(index)));
    }
    final int remainingCount = piece.count - matchingCount;
    if (remainingCount >= MIN_COUNT) {
      pieces.push(new Piece(piece.open, piece.begin, remainingCount));
    }
    return index + matchingCount;
  }

  private int countRun(final int index, final char character, final int maxCount) {
    int count = 0;
    while (count < maxCount && index + count < end && text.charAt(index + count) == character) {
      count++;
    }
    return count;
  }

  private static final class Piece {

    private final char open;
    private final int begin;
    private final int count;
    private final List<Integer> separators;
    private final List<Integer> equals;

    private Piece(final char open, final int begin, final int count) {
      this.open = open;
      this.begin = begin;
      this.count = count;
      this.separators = new ArrayList<>();
      this.equals = new ArrayList<>();
      this.equals.add(CurlyBrackets.Part.NO_EQUAL);
    }

    private char close() {
      return switch (open) {
        case CURLY_OPEN -> CURLY_CLOSE;
        case EQUAL -> NEW_LINE;
        default -> SQUARE_CLOSE;
      };
    }

    private boolean canMarkEqual() {
      return open == CURLY_OPEN
          && equals.size() > 1
          && equals.getLast() == CurlyBrackets.Part.NO_EQUAL;
    }

    private int maxCount() {
      return open == CURLY_OPEN ? MAX_CURLY_COUNT : MIN_COUNT;
    }

    private void addPart(final int separator) {
      separators.add(separator);
      equals.add(CurlyBrackets.Part.NO_EQUAL);
    }

    private void markEqual(final int index) {
      final int lastPart = equals.size() - 1;
      if (lastPart > 0 && equals.get(lastPart) == CurlyBrackets.Part.NO_EQUAL) {
        equals.set(lastPart, index);
      }
    }

    private List<CurlyBrackets.Part> parts(final int partsEnd) {
      final List<CurlyBrackets.Part> parts = new ArrayList<>();
      int partBegin = begin + count;
      for (int part = 0; part < separators.size(); part++) {
        parts.add(new CurlyBrackets.Part(partBegin, separators.get(part), equals.get(part)));
        partBegin = separators.get(part) + 1;
      }
      parts.add(new CurlyBrackets.Part(partBegin, partsEnd, equals.getLast()));
      return parts;
    }
  }
}
