package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.move.ApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQuery;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.repository.token.TokenRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiEditWrapper implements DisposableBean {

  private final ApiEdit apiEdit;
  private final ApiMove apiMove;
  private final TagRepository tagRepository;
  private final TokenRepository tokenRepository;
  private final CurrentUserService user;
  private final Duration throttlePeriod;
  private final Deque<Instant> callTimes;
  private final ExecutorService executor;
  private final List<String> tags;
  private String prefixWithoutTags;
  private String prefixWithTag;

  public ApiEditWrapper(
      final ApiEdit apiEdit,
      final ApiMove apiMove,
      final TagRepository tagRepository,
      final TokenRepository tokenRepository,
      final CurrentUserService user,
      @Value("${wpcleaner.api.edit-wrapper.throttle-period-seconds:60}")
          final long throttlePeriodSeconds) {
    this.apiEdit = apiEdit;
    this.apiMove = apiMove;
    this.tagRepository = tagRepository;
    this.tokenRepository = tokenRepository;
    this.user = user;
    this.throttlePeriod = Duration.ofSeconds(throttlePeriodSeconds);
    this.callTimes = new ArrayDeque<>();
    this.executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              final Thread thread = new Thread(runnable, "ApiEditWrapper");
              thread.setDaemon(true);
              return thread;
            });
    this.tags = new ArrayList<>();
    this.prefixWithoutTags = "";
    this.prefixWithTag = "";
  }

  public void setTags(final List<String> newTags) {
    tags.clear();
    tags.addAll(newTags);
  }

  public void setPrefixes(final String withoutTags, final String withTag) {
    prefixWithoutTags = withoutTags;
    prefixWithTag = withTag;
  }

  public Edit edit(final WikiDefinition wiki, final EditQuery query) {
    executor.execute(this::throttle);
    try {
      return executor.submit(() -> editInternal(wiki, query)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw ApiException.of(e, "Error editing page with %s".formatted(query.description()));
    }
  }

  private Edit editInternal(final WikiDefinition wiki, final EditQuery query) {
    final EditQueryCommon.Builder builder = query.common().builder();
    final List<String> tagsToUse = computeTags();
    builder.tags(tagsToUse);
    final String prefix = tagsToUse.isEmpty() ? prefixWithoutTags : prefixWithTag;
    if (!prefix.isBlank()) {
      builder.summary("%s - %s".formatted(prefix, query.common().summary()));
    }
    builder.token(tokenRepository.getCsrf(wiki));
    return apiEdit.edit(wiki, query.withCommon(builder.build()));
  }

  public Move move(final WikiDefinition wiki, final MoveQuery query) {
    executor.execute(this::throttle);
    try {
      return executor.submit(() -> moveInternal(wiki, query)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw ApiException.of(e, "Error moving page %s".formatted(query.description()));
    }
  }

  private Move moveInternal(final WikiDefinition wiki, final MoveQuery query) {
    final MoveQueryCommon.Builder builder = query.common().builder();
    final List<String> tagsToUse = computeTags();
    builder.tags(tagsToUse);
    final String prefix = tagsToUse.isEmpty() ? prefixWithoutTags : prefixWithTag;
    if (!prefix.isBlank() && query.common().reason() != null) {
      builder.reason("%s - %s".formatted(prefix, query.common().reason()));
    }
    builder.token(tokenRepository.getCsrf(wiki));
    return apiMove.move(wiki, query.withCommon(builder.build()));
  }

  private List<String> computeTags() {
    return tags.stream()
        .map(tagRepository::getSimilarTag)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Tag::name)
        .findFirst()
        .map(List::of)
        .orElse(List.of());
  }

  private void throttle() {
    removeExpiredCallTimes();
    if (callTimes.size() >= computeMaxEdits()) {
      waitForFirstCallTimeExpiry();
      removeExpiredCallTimes();
    }
    callTimes.add(Instant.now());
  }

  private void removeExpiredCallTimes() {
    final Instant threshold = Instant.now().minus(throttlePeriod);
    callTimes.removeIf(threshold::isAfter);
  }

  private void waitForFirstCallTimeExpiry() {
    final Duration wait =
        Duration.between(Instant.now(), callTimes.getFirst().plus(throttlePeriod));
    if (wait.isNegative()) {
      return;
    }
    try {
      Thread.sleep(wait);
    } catch (InterruptedException e) {
      throw ApiException.of(e, "Error waiting for throttle");
    }
  }

  private int computeMaxEdits() {
    final ConnectedUser currentUser = user.getCurrentUser();
    if (currentUser.demo() || !currentUser.isLoggedIn()) {
      return 1;
    }
    if (currentUser.groups().contains("bot")) {
      return Integer.MAX_VALUE;
    }
    return 4;
  }

  @Override
  public void destroy() {
    executor.shutdownNow();
  }
}
