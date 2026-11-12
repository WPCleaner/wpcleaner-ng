package org.wpcleaner.api.analysis.timeline;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;

public class TimelineContainer {

  private final String text;
  private final PreprocessorContainer preprocessor;
  private final List<TimelineLinkElement> links;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public TimelineContainer(final String text, final PreprocessorContainer preprocessor) {
    this.text = text;
    this.preprocessor = preprocessor;
    this.links = new ArrayList<>();
    this.done = false;
  }

  public List<TimelineLinkElement> getLinks() {
    lock.lock();
    try {
      if (!done) {
        links.addAll(new TimelineAnalyzer(text).analyze(preprocessor.getCompleteTags()));
        done = true;
      }
    } finally {
      lock.unlock();
    }
    return links;
  }
}
