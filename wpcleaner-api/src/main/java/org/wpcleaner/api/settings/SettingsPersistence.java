package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.utils.JsonUtils;

@Service
public class SettingsPersistence {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public boolean arePersisted(final VersionedSettings settings) {
    return getFolder().resolve("%s.json".formatted(settings.name())).toFile().isFile();
  }

  public void save(final VersionedSettings settings) {
    final Path file = getFolder().resolve("%s.json".formatted(settings.name()));
    JsonUtils.writeValue(file.toFile(), settings);
  }

  private Path getFolder() {
    final Path path = Paths.get(System.getProperty("user.home")).resolve(".wpcleaner");
    if (!path.toFile().mkdirs()) {
      LOGGER.error("Unable to create folder {}", path);
    }
    return path;
  }
}
