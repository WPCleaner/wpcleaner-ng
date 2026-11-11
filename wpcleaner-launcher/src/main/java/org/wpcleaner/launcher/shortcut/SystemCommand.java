package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class SystemCommand {

  private static final long TIMEOUT_SECONDS = 30;

  private SystemCommand() {}

  static String run(final List<String> command, final Map<String, String> environment)
      throws IOException {
    final ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
    builder.environment().putAll(environment);
    final Process process = builder.start();
    try (InputStream output = process.getInputStream()) {
      final String text = new String(output.readAllBytes(), StandardCharsets.UTF_8).strip();
      if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new IOException("Timeout running %s".formatted(command.getFirst()));
      }
      if (process.exitValue() != 0) {
        throw new IOException(
            "%s failed with exit code %d: %s"
                .formatted(command.getFirst(), process.exitValue(), text));
      }
      return text;
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted running %s".formatted(command.getFirst()), e);
    }
  }
}
