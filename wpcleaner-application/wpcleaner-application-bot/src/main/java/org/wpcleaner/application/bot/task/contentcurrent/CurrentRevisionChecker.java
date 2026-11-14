package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.bot.content.ContentPage;
import org.wpcleaner.application.bot.request.RequestLimits;
import org.wpcleaner.application.bot.request.RequestThrottle;

public final class CurrentRevisionChecker implements Callable<Void> {

  public static final RevisionsQuery CURRENT_CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(
              Set.of(
                  RevisionsParameters.Properties.CONTENT,
                  RevisionsParameters.Properties.CONTENT_MODEL))
          .slots(Set.of(Slot.MAIN))
          .build();

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int MAX_ATTEMPTS = 3;
  private static final long POLL_SECONDS = 1;

  private final WikiDefinition wiki;
  private final ApiRevisions apiRevisions;
  private final PageErrorsDetector detector;
  private final PageErrorsWriter writer;
  private final int maxPageIds;
  private final RequestThrottle throttle;
  private final BlockingQueue<Integer> pageIds = new LinkedBlockingQueue<>();
  private final AtomicLong checked = new AtomicLong();
  private final AtomicLong withErrors = new AtomicLong();
  private final AtomicBoolean finished = new AtomicBoolean();

  public CurrentRevisionChecker(
      final WikiDefinition wiki,
      final ApiRevisions apiRevisions,
      final PageErrorsDetector detector,
      final PageErrorsWriter writer,
      final RequestLimits limits) {
    this.wiki = wiki;
    this.apiRevisions = apiRevisions;
    this.detector = detector;
    this.writer = writer;
    this.maxPageIds = limits.maxPageIds();
    this.throttle = new RequestThrottle(limits.interval());
  }

  public void add(final int pageId) {
    pageIds.add(pageId);
  }

  public void finish() {
    finished.set(true);
  }

  public int getPendingPages() {
    return pageIds.size();
  }

  public long getCheckedPages() {
    return checked.get();
  }

  public long getPagesWithErrors() {
    return withErrors.get();
  }

  @Override
  public Void call() throws IOException, InterruptedException {
    final List<Integer> batch = new ArrayList<>(maxPageIds);
    while (!isCompleted(batch)) {
      collect(batch);
      if (isReady(batch)) {
        check(List.copyOf(batch));
        batch.clear();
      }
    }
    return null;
  }

  private boolean isCompleted(final List<Integer> batch) {
    return finished.get() && pageIds.isEmpty() && batch.isEmpty();
  }

  private boolean isReady(final List<Integer> batch) {
    return batch.size() >= maxPageIds || (finished.get() && pageIds.isEmpty() && !batch.isEmpty());
  }

  private void collect(final List<Integer> batch) throws InterruptedException {
    final Integer pageId = pageIds.poll(POLL_SECONDS, TimeUnit.SECONDS);
    if (pageId != null) {
      batch.add(pageId);
      pageIds.drainTo(batch, maxPageIds - batch.size());
    }
  }

  private void check(final List<Integer> batch) throws IOException, InterruptedException {
    for (final Page page : retrieveCurrentPages(batch)) {
      final Optional<ContentPage> current = toContentPage(page);
      if (current.isPresent()) {
        checkPage(current.get());
      }
    }
    checked.addAndGet(batch.size());
  }

  private List<Page> retrieveCurrentPages(final List<Integer> batch) throws InterruptedException {
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      throttle.await();
      try {
        return apiRevisions.retrieveRevisionsByPageId(wiki, batch, CURRENT_CONTENT_QUERY);
      } catch (final RestClientException | ApiException e) {
        LOGGER.warn(
            "Attempt {} to retrieve the current revisions of {} pages failed: {}",
            attempt,
            batch.size(),
            e.getMessage());
      }
    }
    LOGGER.error("Unable to retrieve the current revisions of pages {}", batch);
    return List.of();
  }

  private static Optional<ContentPage> toContentPage(final Page page) {
    final Integer pageId = page.pageId();
    final Integer namespace = page.ns();
    final String title = page.title();
    if (page.missing() || pageId == null || namespace == null || title == null) {
      return Optional.empty();
    }
    final Optional<RevisionSlot> slot =
        page.revisions().stream().findFirst().flatMap(Revision::mainSlot);
    final String content = slot.map(RevisionSlot::content).orElse(null);
    if (content == null) {
      return Optional.empty();
    }
    final String model =
        Objects.requireNonNullElse(
            slot.map(RevisionSlot::contentModel).orElse(null), ContentPage.WIKITEXT_MODEL);
    return Optional.of(new ContentPage(pageId, namespace, title, model, content));
  }

  private void checkPage(final ContentPage page) throws IOException {
    final List<ReportedError> errors = detector.detect(page);
    if (!errors.isEmpty()) {
      writer.write(new PageErrors(page.id(), page.namespace(), page.title(), errors));
      withErrors.incrementAndGet();
    }
  }
}
