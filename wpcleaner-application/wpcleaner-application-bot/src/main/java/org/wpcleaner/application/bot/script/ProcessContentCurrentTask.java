package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public record ProcessContentCurrentTask(Set<Integer> namespaces, List<ReportedAlgorithm> report)
    implements BotTask {

  public static final String TYPE = "processContentCurrent";
  public static final Set<Integer> DEFAULT_NAMESPACES = Set.of(0);

  @JsonCreator
  public ProcessContentCurrentTask(
      @JsonProperty("namespaces") @Nullable final Set<Integer> namespaces,
      @JsonProperty("report") @Nullable final List<ReportedAlgorithm> report) {
    this.namespaces = Set.copyOf(Objects.requireNonNullElse(namespaces, DEFAULT_NAMESPACES));
    this.report = List.copyOf(Objects.requireNonNullElseGet(report, List::of));
  }

  @Override
  public Set<Integer> algorithms() {
    return report.stream()
        .map(ReportedAlgorithm::algorithm)
        .collect(Collectors.toUnmodifiableSet());
  }

  public Set<Integer> namespaces(final ReportedAlgorithm algorithm) {
    return Objects.requireNonNullElse(algorithm.namespaces(), namespaces);
  }
}
