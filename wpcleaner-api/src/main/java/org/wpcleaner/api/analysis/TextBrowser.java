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
    this.exclusions.addAll(excludes);
    this.exclusions.sort(Element.COMPARATOR);
  }

  public Cursor cursor() {
    return new Cursor();
  }

  public final class Cursor {

    private int index;
    private boolean jump;
    private int currentExclude;

    private Cursor() {
      this.currentExclude = 0;
      this.jump = false;
      this.index = computeAfter(-1);
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
}
