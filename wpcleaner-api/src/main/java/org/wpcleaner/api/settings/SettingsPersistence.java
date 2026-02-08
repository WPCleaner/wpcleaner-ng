package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.utils.JsonUtils;

@Service
public class SettingsPersistence {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public <T extends VersionedSettings> Optional<T> load(final Class<T> clazz) {
    final FileSystemResource file = new FileSystemResource(getFile(clazz));
    if (!file.exists() || !file.isReadable()) {
      return Optional.empty();
    }
    return Optional.of(JsonUtils.readValue(new FileSystemResource(getFile(clazz)), clazz));
  }

  public void save(final VersionedSettings settings) {
    JsonUtils.writeValue(getFile(settings.getClass()).toFile(), settings);
  }

  private <T extends VersionedSettings> Path getFile(final Class<T> clazz) {
    return getFolder().resolve("%s.json".formatted(VersionedSettings.name(clazz)));
  }

  private Path getFolder() {
    final Path path = Paths.get(System.getProperty("user.home")).resolve(".wpcleaner");
    if (!path.toFile().mkdirs()) {
      LOGGER.error("Unable to create folder {}", path);
    }
    return path;
  }
}
