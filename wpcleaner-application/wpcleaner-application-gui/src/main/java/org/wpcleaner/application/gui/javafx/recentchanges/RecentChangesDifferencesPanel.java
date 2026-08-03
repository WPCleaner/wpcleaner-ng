package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;

public final class RecentChangesDifferencesPanel extends HBox {

  private static final StyleAttributeMap RED_BACKGROUND =
      StyleAttributeMap.builder().setBackground(Color.web("#ffcccc")).build();

  private static final StyleAttributeMap GREEN_BACKGROUND =
      StyleAttributeMap.builder().setBackground(Color.web("#ccffcc")).build();

  private static final char NEWLINE = '\n';

  private final RichTextArea oldContentArea;
  private final RichTextArea newContentArea;
  private final List<AbstractDelta<Character>> deltas;
  private @Nullable String currentContent;
  private @Nullable String currentOldContent;
  private boolean isSyncingScroll;
  private boolean isScrollSynced;
  private int currentDeltaIndex = -1;

  public RecentChangesDifferencesPanel() {
    super(10);

    this.deltas = new ArrayList<>();

    this.oldContentArea = new RichTextArea();
    this.oldContentArea.setEditable(false);
    this.oldContentArea.setWrapText(true);
    this.oldContentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    final Label previousLabel = new Label(GT._T("Previous revision"));
    final VBox leftBox = new VBox(5);
    leftBox.getChildren().addAll(previousLabel, oldContentArea);
    VBox.setVgrow(oldContentArea, Priority.ALWAYS);
    setHgrow(leftBox, Priority.ALWAYS);

    this.newContentArea = new RichTextArea();
    this.newContentArea.setEditable(false);
    this.newContentArea.setWrapText(true);
    this.newContentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    final Label newLabel = new Label(GT._T("New revision"));
    final VBox rightBox = new VBox(5);
    rightBox.getChildren().addAll(newLabel, newContentArea);
    VBox.setVgrow(newContentArea, Priority.ALWAYS);
    setHgrow(rightBox, Priority.ALWAYS);

    getChildren().addAll(leftBox, rightBox);
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();
    setupScrollSync();
  }

  private void setupScrollSync() {
    if (isScrollSynced) {
      return;
    }
    final ScrollBar oldVBar = findVerticalScrollBar(oldContentArea);
    final ScrollBar newVBar = findVerticalScrollBar(newContentArea);
    if (oldVBar != null && newVBar != null) {
      isScrollSynced = true;
      bindScrollBars(oldVBar, newVBar);
      bindScrollBars(newVBar, oldVBar);
    }
  }

  @SuppressWarnings("PMD.UnusedAssignment")
  private void bindScrollBars(final ScrollBar sourceBar, final ScrollBar targetBar) {
    sourceBar
        .valueProperty()
        .addListener(
            (_, _, newValue) -> {
              if (!isSyncingScroll) {
                isSyncingScroll = true;
                try {
                  final double max = sourceBar.getMax();
                  final double relativeValue = max > 0 ? newValue.doubleValue() / max : 0;
                  targetBar.setValue(relativeValue * targetBar.getMax());
                } finally {
                  isSyncingScroll = false;
                }
              }
            });
  }

  private @Nullable ScrollBar findVerticalScrollBar(final Parent parent) {
    for (final Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof ScrollBar scrollBar
          && scrollBar.getOrientation() == Orientation.VERTICAL) {
        return scrollBar;
      }
      if (node instanceof Parent parentNode) {
        final ScrollBar scrollBar = findVerticalScrollBar(parentNode);
        if (scrollBar != null) {
          return scrollBar;
        }
      }
    }
    return null;
  }

  private TextPos getTextPos(@Nullable final String text, final int flatOffset) {
    if (text == null || flatOffset <= 0) {
      return TextPos.ZERO;
    }
    int paragraphIndex = 0;
    int currentOffset = 0;
    for (int i = 0; i < text.length() && i < flatOffset; i++) {
      if (text.charAt(i) == NEWLINE) {
        paragraphIndex++;
        currentOffset = 0;
      } else {
        currentOffset++;
      }
    }
    return TextPos.ofLeading(paragraphIndex, currentOffset);
  }

  @SuppressWarnings("PMD.NullAssignment")
  public void clear() {
    oldContentArea.clear();
    newContentArea.clear();
    currentContent = null;
    currentOldContent = null;
    currentDeltaIndex = -1;
  }

  public void updateContents(
      @Nullable final String content,
      @Nullable final String oldContent,
      final List<AbstractDelta<Character>> deltas) {
    this.deltas.clear();
    this.deltas.addAll(deltas);
    this.currentContent = content;
    this.currentOldContent = oldContent;
    this.currentDeltaIndex = -1;
    if (content != null) {
      newContentArea.clear();
      newContentArea.appendText(content, StyleAttributeMap.EMPTY);
    }
    if (oldContent != null) {
      oldContentArea.clear();
      oldContentArea.appendText(oldContent, StyleAttributeMap.EMPTY);
    }

    if (content != null && oldContent != null) {
      applyDeltaStyles(deltas);
    } else if (content != null) {
      newContentArea.applyStyle(TextPos.ZERO, newContentArea.getDocumentEnd(), GREEN_BACKGROUND);
    }
  }

  private void applyDeltaStyles(final List<AbstractDelta<Character>> deltas) {
    for (final AbstractDelta<Character> delta : deltas) {
      final DeltaType type = delta.getType();
      if (type == DeltaType.DELETE || type == DeltaType.CHANGE) {
        final int start = delta.getSource().getPosition();
        final int length = delta.getSource().getLines().size();
        final TextPos startPos = getTextPos(currentOldContent, start);
        final TextPos endPos = getTextPos(currentOldContent, start + length);
        oldContentArea.applyStyle(startPos, endPos, RED_BACKGROUND);
      }
      if (type == DeltaType.INSERT || type == DeltaType.CHANGE) {
        final int start = delta.getTarget().getPosition();
        final int length = delta.getTarget().getLines().size();
        final TextPos startPos = getTextPos(currentContent, start);
        final TextPos endPos = getTextPos(currentContent, start + length);
        newContentArea.applyStyle(startPos, endPos, GREEN_BACKGROUND);
      }
    }
  }

  private void selectDelta(final int index) {
    if (deltas.isEmpty()) {
      return;
    }
    if (index < 0 || index >= deltas.size()) {
      return;
    }
    currentDeltaIndex = index;
    final AbstractDelta<Character> delta = deltas.get(index);
    isSyncingScroll = true;
    try {
      final int oldStart = delta.getSource().getPosition();
      final int oldLength = delta.getSource().getLines().size();
      final TextPos oldStartPos = getTextPos(currentOldContent, oldStart);
      final TextPos oldEndPos = getTextPos(currentOldContent, oldStart + oldLength);
      oldContentArea.select(oldStartPos, oldEndPos);

      final int newStart = delta.getTarget().getPosition();
      final int newLength = delta.getTarget().getLines().size();
      final TextPos newStartPos = getTextPos(currentContent, newStart);
      final TextPos newEndPos = getTextPos(currentContent, newStart + newLength);
      newContentArea.select(newStartPos, newEndPos);
    } finally {
      isSyncingScroll = false;
    }
  }

  public void selectFirstDelta() {
    if (deltas.isEmpty()) {
      return;
    }
    selectDelta(0);
  }

  public void selectPreviousDelta() {
    if (deltas.isEmpty()) {
      return;
    }
    int targetIndex = currentDeltaIndex - 1;
    if (targetIndex < 0) {
      targetIndex = deltas.size() - 1;
    }
    selectDelta(targetIndex);
  }

  public void selectNextDelta() {
    if (deltas.isEmpty()) {
      return;
    }
    int targetIndex = currentDeltaIndex + 1;
    if (targetIndex >= deltas.size()) {
      targetIndex = 0;
    }
    selectDelta(targetIndex);
  }

  public void selectLastDelta() {
    if (deltas.isEmpty()) {
      return;
    }
    selectDelta(deltas.size() - 1);
  }
}
