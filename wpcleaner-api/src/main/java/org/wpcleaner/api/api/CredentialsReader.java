package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public interface CredentialsReader {

  List<Credential> getCredentials();

  default Optional<Resource> findResource(final String filename) {
    Resource resource = new ClassPathResource(filename);
    if (resource.exists() && resource.isFile() && resource.isReadable()) {
      return Optional.of(resource);
    }
    resource =
        new FileSystemResource(Path.of(System.getProperty("user.home"), ".wpcleaner", filename));
    if (resource.exists() && resource.isFile() && resource.isReadable()) {
      return Optional.of(resource);
    }
    return Optional.empty();
  }
}
