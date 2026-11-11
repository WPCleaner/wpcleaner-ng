package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.OptionalInt;
import org.wpcleaner.launcher.start.ProcessStarter;
import org.wpcleaner.launcher.start.StartedProcess;

final class RecordingProcessStarter implements ProcessStarter {

  private final List<List<String>> commands = new ArrayList<>();
  private final Deque<Integer> exitCodes = new ArrayDeque<>();

  void exitNextWith(final int exitCode) {
    exitCodes.add(exitCode);
  }

  List<List<String>> commands() {
    return List.copyOf(commands);
  }

  @Override
  public StartedProcess start(
      final List<String> command, final Path workingDirectory, final Path output) {
    commands.add(List.copyOf(command));
    final Integer exitCode = exitCodes.poll();
    return _ -> exitCode == null ? OptionalInt.empty() : OptionalInt.of(exitCode);
  }
}
