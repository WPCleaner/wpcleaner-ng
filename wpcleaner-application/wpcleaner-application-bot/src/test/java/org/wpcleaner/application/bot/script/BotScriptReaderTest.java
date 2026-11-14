package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.bot.BotSpringBootTest;

@BotSpringBootTest
class BotScriptReaderTest {

  @Autowired private BotScriptReader reader;

  @TempDir private Path folder;

  @DisplayName("should read the tasks of a script with their algorithms and namespaces")
  @Test
  void readScript() throws IOException {
    // GIVEN
    final Path file =
        givenScript(
            """
            # Comments are allowed
            tasks:
              - type: processContentCurrent
                report:
                  - 17
                  - algorithm: 48
                    namespaces: [0, 104]
              - type: processContentCurrent
                namespaces: [0, 14]
                report: [64]
            """);

    // WHEN
    final BotScript script = reader.read(file);

    // THEN
    Assertions.assertThat(script.tasks())
        .containsExactly(
            new ProcessContentCurrentTask(
                Set.of(0),
                List.of(ReportedAlgorithm.of(17), ReportedAlgorithm.of(48, Set.of(0, 104)))),
            new ProcessContentCurrentTask(Set.of(0, 14), List.of(ReportedAlgorithm.of(64))));
  }

  @DisplayName("should give the namespaces of each algorithm, defaulting to the task ones")
  @Test
  void namespaces() throws IOException {
    // GIVEN
    final Path file =
        givenScript(
            """
            tasks:
              - type: processContentCurrent
                namespaces: [0, 14]
                report: [17, {algorithm: 48, namespaces: [104]}]
            """);
    final ProcessContentCurrentTask task =
        (ProcessContentCurrentTask) reader.read(file).tasks().getFirst();

    // WHEN
    final List<Set<Integer>> namespaces = task.report().stream().map(task::namespaces).toList();

    // THEN
    Assertions.assertThat(namespaces).containsExactly(Set.of(0, 14), Set.of(104));
    Assertions.assertThat(task.algorithms()).containsExactlyInAnyOrder(17, 48);
  }

  @DisplayName("should reject scripts with an unknown type of task")
  @Test
  void readUnknownTask() throws IOException {
    // GIVEN
    final Path file =
        givenScript(
            """
            tasks:
              - type: unknownTask
            """);

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> reader.read(file)).isInstanceOf(IOException.class);
  }

  @DisplayName("should reject scripts without tasks")
  @Test
  void readWithoutTasks() throws IOException {
    // GIVEN
    final Path file = givenScript("other: 1\n");

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> reader.read(file)).isInstanceOf(IOException.class);
  }

  private Path givenScript(final String content) throws IOException {
    final Path file = folder.resolve("script.yaml");
    Files.writeString(file, content, StandardCharsets.UTF_8);
    return file;
  }
}
