package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record BotScript(List<BotTask> tasks) {
  @JsonCreator
  public BotScript(@JsonProperty(value = "tasks", required = true) final List<BotTask> tasks) {
    this.tasks = List.copyOf(tasks);
  }

  public Set<Integer> algorithms() {
    return tasks.stream()
        .flatMap(task -> task.algorithms().stream())
        .collect(Collectors.toUnmodifiableSet());
  }
}
