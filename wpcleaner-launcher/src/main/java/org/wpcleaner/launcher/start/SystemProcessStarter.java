package org.wpcleaner.launcher.start;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

public final class SystemProcessStarter implements ProcessStarter {

  @Override
  public StartedProcess start(
      final List<String> command, final Path workingDirectory, final Path output)
      throws IOException {
    final Process process =
        new ProcessBuilder(command)
            .directory(workingDirectory.toFile())
            .redirectErrorStream(true)
            .redirectOutput(Redirect.appendTo(output.toFile()))
            .start();
    return timeout ->
        process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
            ? OptionalInt.of(process.exitValue())
            : OptionalInt.empty();
  }
}
