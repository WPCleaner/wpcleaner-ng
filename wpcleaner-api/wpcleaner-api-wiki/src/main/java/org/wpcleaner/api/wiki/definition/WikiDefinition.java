package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.ComponentOrientation;
import java.util.Set;
import org.wpcleaner.lib.image.ImageCollection;

public record WikiDefinition(
    String language,
    String name,
    ImageCollection image,
    WikiGroup group,
    Set<String> hosts,
    String apiPath,
    String indexPath,
    String code,
    @Nullable String checkWikiCode,
    ComponentOrientation orientation,
    @Nullable WikiWarning warning) {

  @Override
  public String toString() {
    return "%s - %s".formatted(code, name);
  }
}
