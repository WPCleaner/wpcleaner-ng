package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.tag.TagRepository;
import org.wpcleaner.api.repository.token.TokenRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiEditWrapper implements DisposableBean {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Duration THROTTLE_PERIOD = Duration.ofMinutes(1);

  private final ApiEdit apiEdit;
  private final TagRepository tagRepository;
  private final TokenRepository tokenRepository;
  private final CurrentUserService user;
  private final Deque<Instant> callTimes;
  private final ExecutorService executor;
  private final List<String> tags;
  private String prefixWithoutTags;
  private String prefixWithTag;

  public ApiEditWrapper(
      final ApiEdit apiEdit,
      final TagRepository tagRepository,
      final TokenRepository tokenRepository,
      final CurrentUserService user) {
    this.apiEdit = apiEdit;
    this.tagRepository = tagRepository;
    this.tokenRepository = tokenRepository;
    this.user = user;
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
      final String message = "Error editing page with %s".formatted(query.description());
      LOGGER.error(message, e);
      throw new ApiException(message, e, Objects.requireNonNullElse(e.getMessage(), ""));
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
    final Instant threshold = Instant.now().minus(THROTTLE_PERIOD);
    callTimes.removeIf(threshold::isAfter);
    if (callTimes.size() < computeMaxEdits()) {
      return;
    }
    final Duration wait = Duration.between(threshold, callTimes.getFirst());
    if (wait.isNegative()) {
      return;
    }
    try {
      Thread.sleep(wait);
      callTimes.add(Instant.now());
    } catch (InterruptedException e) {
      throw new ApiException(
          "Error waiting for throttle", e, Objects.requireNonNullElse(e.getMessage(), ""));
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
