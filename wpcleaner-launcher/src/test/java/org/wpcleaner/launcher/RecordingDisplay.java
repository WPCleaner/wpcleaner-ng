package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.wpcleaner.launcher.display.LauncherDisplay;

final class RecordingDisplay implements LauncherDisplay {

  private final List<String> informations = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();
  private final List<String> questions = new ArrayList<>();
  private boolean answer = true;
  private boolean progressOpen;

  void answerWith(final boolean newAnswer) {
    answer = newAnswer;
  }

  boolean isProgressOpen() {
    return progressOpen;
  }

  List<String> informations() {
    return List.copyOf(informations);
  }

  List<String> errors() {
    return List.copyOf(errors);
  }

  List<String> questions() {
    return List.copyOf(questions);
  }

  @Override
  public void progress(final String status, final long current, final long total) {
    progressOpen = true;
  }

  @Override
  public void closeProgress() {
    progressOpen = false;
  }

  @Override
  public void inform(final String message) {
    informations.add(message);
  }

  @Override
  public void error(final String message) {
    errors.add(message);
  }

  @Override
  public boolean confirm(final String message) {
    questions.add(message);
    return answer;
  }
}
