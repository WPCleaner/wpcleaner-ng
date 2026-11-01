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

final class BracketStack {

  static final char CURLY_OPEN = '{';
  static final char CURLY_CLOSE = '}';
  static final char SQUARE_OPEN = '[';
  static final char SQUARE_CLOSE = ']';
  static final char PIPE = '|';
  static final char EQUAL = '=';
  private static final int MIN_COUNT = 2;
  private static final int MAX_CURLY_COUNT = 3;

  private final String text;
  private final int end;
  private final Consumer<CurlyBrackets> curlyBracketsConsumer;
  private final Deque<Piece> pieces;

  BracketStack(
      final String text, final int end, final Consumer<CurlyBrackets> curlyBracketsConsumer) {
    this.text = text;
    this.end = end;
    this.curlyBracketsConsumer = curlyBracketsConsumer;
    this.pieces = new ArrayDeque<>();
  }

  int analyze(final int index) {
    final char current = text.charAt(index);
    if (current == CURLY_OPEN || current == SQUARE_OPEN) {
      return open(index, current);
    }
    final Piece top = pieces.peek();
    if (top == null) {
      return index + 1;
    }
    if (current == top.close()) {
      return close(index, top);
    }
    if (top.open == CURLY_OPEN && current == PIPE) {
      top.addPart(index);
    } else if (top.open == CURLY_OPEN && current == EQUAL) {
      top.markEqual(index);
    }
    return index + 1;
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
      return open == CURLY_OPEN ? CURLY_CLOSE : SQUARE_CLOSE;
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
