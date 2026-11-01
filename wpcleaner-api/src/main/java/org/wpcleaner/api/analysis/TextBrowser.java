package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;

public final class TextBrowser {

  private final String text;
  private final List<Element> exclusions;

  public TextBrowser(final String text) {
    this.text = text;
    this.exclusions = new ArrayList<>();
  }

  public void addExclusions(final List<? extends Element> excludes) {
    final List<Element> sortedExclusions = new ArrayList<>(exclusions);
    sortedExclusions.addAll(excludes);
    sortedExclusions.sort(Element.COMPARATOR);
    exclusions.clear();
    for (final Element exclusion : sortedExclusions) {
      final int lastIndex = exclusions.size() - 1;
      if (lastIndex >= 0 && exclusion.begin() <= exclusions.get(lastIndex).end()) {
        final Element last = exclusions.get(lastIndex);
        exclusions.set(
            lastIndex, new Exclusion(last.begin(), Math.max(last.end(), exclusion.end())));
      } else {
        exclusions.add(new Exclusion(exclusion.begin(), exclusion.end()));
      }
    }
  }

  public Cursor cursor() {
    return new Cursor(0);
  }

  public Cursor cursor(final int index) {
    return new Cursor(index);
  }

  public boolean isInsideExclusion(final int index) {
    int low = 0;
    int high = exclusions.size() - 1;
    while (low <= high) {
      final int middle = (low + high) >>> 1;
      final Element exclusion = exclusions.get(middle);
      if (index <= exclusion.begin()) {
        high = middle - 1;
      } else if (index >= exclusion.end()) {
        low = middle + 1;
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean isContinuous(final int begin, final int length) {
    final Cursor cursor = new Cursor(begin);
    for (int offset = 1; offset < length; offset++) {
      cursor.moveNext();
      if (!cursor.samePart() || cursor.getIndex() != begin + offset) {
        return false;
      }
    }
    return true;
  }

  public ReverseCursor reverseCursor() {
    return new ReverseCursor();
  }

  public final class Cursor {

    private int index;
    private boolean jump;
    private int currentExclude;

    private Cursor(final int start) {
      this.currentExclude = 0;
      this.jump = false;
      this.index = computeAfter(start - 1);
    }

    public int getIndex() {
      return index;
    }

    public boolean samePart() {
      return !jump;
    }

    public void moveNext() {
      jump = false;
      index = computeAfter(index);
    }

    public void moveAfterWhitespace() {
      jump = false;
      while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
        index = computeAfter(index);
      }
    }

    public void moveAfterUntil(final String unauthorized) {
      jump = false;
      while (index < text.length() && unauthorized.indexOf(text.charAt(index)) < 0) {
        index = computeAfter(index);
      }
    }

    private int computeAfter(final int start) {
      int result = start + 1;
      while (currentExclude < exclusions.size()
          && result >= exclusions.get(currentExclude).begin()) {
        result = Math.max(result, exclusions.get(currentExclude).end());
        if (!jump) {
          jump = true;
        }
        currentExclude++;
      }
      return result;
    }
  }

  public final class ReverseCursor {

    private int index;
    private boolean jump;
    private int currentExclude;

    private ReverseCursor() {
      this.currentExclude = exclusions.size() - 1;
      this.jump = false;
      this.index = computeBefore(text.length());
    }

    public ReverseCursor copy() {
      final ReverseCursor copy = new ReverseCursor();
      copy.index = index;
      copy.jump = jump;
      copy.currentExclude = currentExclude;
      return copy;
    }

    public Cursor toCursor() {
      final Cursor cursor = new Cursor(0);
      cursor.index = index;
      cursor.jump = false;
      cursor.currentExclude = 0;
      for (int exclusion = 0; exclusion < exclusions.size(); exclusion++) {
        if (index > exclusions.get(exclusion).begin()) {
          cursor.currentExclude = exclusion + 1;
        }
      }
      return cursor;
    }

    public int getIndex() {
      return index;
    }

    public boolean samePart() {
      return !jump;
    }

    public void movePrevious() {
      jump = false;
      index = computeBefore(index);
    }

    private int computeBefore(final int start) {
      int result = start - 1;
      while (currentExclude >= 0 && result < exclusions.get(currentExclude).end()) {
        if (result >= exclusions.get(currentExclude).begin()) {
          result = exclusions.get(currentExclude).begin() - 1;
          if (!jump) {
            jump = true;
          }
        }
        currentExclude--;
      }
      return result;
    }
  }

  private record Exclusion(int begin, int end) implements Element {}
}
