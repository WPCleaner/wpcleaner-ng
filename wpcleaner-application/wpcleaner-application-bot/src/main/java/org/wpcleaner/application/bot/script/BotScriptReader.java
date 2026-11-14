package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@Service
public class BotScriptReader {

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final ObjectReader reader = new YAMLMapper().readerFor(BotScript.class);

  public BotScriptReader(final CheckAlgorithmRegistry algorithmRegistry) {
    this.algorithmRegistry = algorithmRegistry;
  }

  public List<BotScript> read(final List<Path> paths) throws IOException {
    final List<BotScript> scripts = new ArrayList<>();
    for (final Path path : paths) {
      scripts.add(read(path));
    }
    return List.copyOf(scripts);
  }

  public BotScript read(final Path path) throws IOException {
    final BotScript script;
    try (InputStream input = Files.newInputStream(path)) {
      script = reader.readValue(input);
    } catch (final IOException e) {
      throw new IOException("Invalid script %s: %s".formatted(path, e.getMessage()), e);
    }
    final List<Integer> unknown =
        script.algorithms().stream()
            .filter(number -> algorithmRegistry.findAlgorithm(number).isEmpty())
            .sorted()
            .toList();
    if (!unknown.isEmpty()) {
      throw new IllegalArgumentException(
          "Unknown algorithms %s in script %s".formatted(unknown, path));
    }
    return script;
  }
}
