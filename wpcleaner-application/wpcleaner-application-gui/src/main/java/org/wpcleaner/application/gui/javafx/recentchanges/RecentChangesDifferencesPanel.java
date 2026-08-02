package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.api.utils.GT;

public final class RecentChangesDifferencesPanel extends HBox {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final InlineCssTextArea oldContentArea;
  private final InlineCssTextArea newContentArea;
  private final List<AbstractDelta<Character>> deltas;
  private boolean isSyncingScroll;
  private int currentDeltaIndex = -1;

  public RecentChangesDifferencesPanel() {
    super(10);

    this.deltas = new ArrayList<>();

    this.oldContentArea = new InlineCssTextArea();
    this.oldContentArea.setEditable(false);
    this.oldContentArea.setWrapText(true);
    this.oldContentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    final VirtualizedScrollPane<InlineCssTextArea> oldContentAreaScrollPane =
        new VirtualizedScrollPane<>(oldContentArea);

    final Label previousLabel = new Label(GT._T("Previous revision"));
    final VBox leftBox = new VBox(5);
    leftBox.getChildren().addAll(previousLabel, oldContentAreaScrollPane);
    VBox.setVgrow(oldContentAreaScrollPane, Priority.ALWAYS);
    setHgrow(leftBox, Priority.ALWAYS);

    this.newContentArea = new InlineCssTextArea();
    this.newContentArea.setEditable(false);
    this.newContentArea.setWrapText(true);
    this.newContentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    final VirtualizedScrollPane<InlineCssTextArea> newContentAreaScrollPane =
        new VirtualizedScrollPane<>(newContentArea);

    final Label newLabel = new Label(GT._T("New revision"));
    final VBox rightBox = new VBox(5);
    rightBox.getChildren().addAll(newLabel, newContentAreaScrollPane);
    VBox.setVgrow(newContentAreaScrollPane, Priority.ALWAYS);
    setHgrow(rightBox, Priority.ALWAYS);

    final ObservableValue<Double> oldScrollY = this.oldContentArea.estimatedScrollYProperty();
    oldScrollY.addListener((_, _, _) -> syncScroll(oldContentArea, newContentArea, true));

    final ObservableValue<Double> newScrollY = this.newContentArea.estimatedScrollYProperty();
    newScrollY.addListener((_, _, _) -> syncScroll(newContentArea, oldContentArea, false));

    getChildren().addAll(leftBox, rightBox);
  }

  private void syncScroll(
      final InlineCssTextArea sourceArea, final InlineCssTextArea targetArea, final boolean newer) {
    if (isSyncingScroll) {
      return;
    }
    isSyncingScroll = true;
    try {
      moveToMatchingParagraph(sourceArea, targetArea, newer);
    } finally {
      isSyncingScroll = false;
    }
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void moveToMatchingParagraph(
      final InlineCssTextArea sourceArea, final InlineCssTextArea targetArea, final boolean newer) {
    if (sourceArea.getLength() == 0) {
      return;
    }
    try {
      final int lastSourcePar = sourceArea.lastVisibleParToAllParIndex();
      final int lastTargetPosition = computeTargetPosition(sourceArea, lastSourcePar, newer);
      final int firstSourcePar = sourceArea.firstVisibleParToAllParIndex();
      final int firstTargetPosition = computeTargetPosition(sourceArea, firstSourcePar, newer);
      targetArea.moveTo((firstTargetPosition + lastTargetPosition) / 2);
      targetArea.requestFollowCaret();
    } catch (Exception e) {
      LOGGER.error("Error in moveToMatchingParagraph: {}", e.getMessage(), e);
    }
  }

  private int computeTargetPosition(
      final InlineCssTextArea sourceArea, final int sourcePar, final boolean newer) {
    int targetPosition =
        IntStream.range(0, sourcePar).map(index -> sourceArea.getText(index).length()).sum()
            + sourcePar;
    for (final AbstractDelta<Character> delta : deltas) {
      final Chunk<Character> sourceChunk = newer ? delta.getSource() : delta.getTarget();
      final Chunk<Character> targetChunk = newer ? delta.getTarget() : delta.getSource();
      if (sourceChunk.getPosition() < targetPosition) {
        targetPosition += targetChunk.size() - sourceChunk.size();
      }
    }
    return targetPosition;
  }

  public void clear() {
    oldContentArea.clear();
    newContentArea.clear();
    currentDeltaIndex = -1;
  }

  public void updateContents(
      @Nullable final String content,
      @Nullable final String oldContent,
      final List<AbstractDelta<Character>> deltas) {
    this.deltas.clear();
    this.deltas.addAll(deltas);
    this.currentDeltaIndex = -1;
    if (content != null) {
      newContentArea.replaceText(content);
      newContentArea.setStyle(0, content.length(), "");
    }
    if (oldContent != null) {
      oldContentArea.replaceText(oldContent);
      oldContentArea.setStyle(0, oldContent.length(), "");
    }

    if (content != null && oldContent != null) {
      applyDeltaStyles(deltas);
    } else if (content != null) {
      newContentArea.setStyle(0, content.length(), "-rtfx-background-color: #ccffcc;");
    }
  }

  private void applyDeltaStyles(final List<AbstractDelta<Character>> deltas) {
    for (final AbstractDelta<Character> delta : deltas) {
      final DeltaType type = delta.getType();
      if (type == DeltaType.DELETE || type == DeltaType.CHANGE) {
        final int start = delta.getSource().getPosition();
        final int length = delta.getSource().getLines().size();
        oldContentArea.setStyle(start, start + length, "-rtfx-background-color: #ffcccc;");
      }
      if (type == DeltaType.INSERT || type == DeltaType.CHANGE) {
        final int start = delta.getTarget().getPosition();
        final int length = delta.getTarget().getLines().size();
        newContentArea.setStyle(start, start + length, "-rtfx-background-color: #ccffcc;");
      }
    }
  }

  @SuppressWarnings("PMD.UnusedAssignment")
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
      oldContentArea.selectRange(oldStart, oldStart + oldLength);
      oldContentArea.requestFollowCaret();

      final int newStart = delta.getTarget().getPosition();
      final int newLength = delta.getTarget().getLines().size();
      newContentArea.selectRange(newStart, newStart + newLength);
      newContentArea.requestFollowCaret();
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
