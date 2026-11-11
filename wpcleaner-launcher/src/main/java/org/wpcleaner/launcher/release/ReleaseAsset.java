package org.wpcleaner.launcher.release;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReleaseAsset(String name, String sha256) {

  private static final Pattern NAME_FORMAT = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,99}");
  private static final Pattern SHA256_FORMAT = Pattern.compile("[0-9a-f]{64}");

  public ReleaseAsset {
    if (name == null || !NAME_FORMAT.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid asset name: %s".formatted(name));
    }
    if (sha256 == null || !SHA256_FORMAT.matcher(sha256).matches()) {
      throw new IllegalArgumentException("Invalid asset checksum: %s".formatted(sha256));
    }
  }
}
