package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.awt.ComponentOrientation;
import java.util.Set;
import org.wpcleaner.lib.image.ImageCollection;

public record WikiDefinition(
    String language,
    String name,
    ImageCollection image,
    WikiGroup group,
    String mainHost,
    Set<String> hosts,
    String apiPath,
    String indexPath,
    String wikiPath,
    String code,
    @Nullable String checkWikiCode,
    ComponentOrientation orientation,
    @Nullable WikiWarning warning) {

  public String apiUrl() {
    return "https://%s/%s".formatted(mainHost, apiPath);
  }

  public String pageUrl(final String title) {
    return "https://%s/%s/%s".formatted(mainHost, wikiPath, title);
  }

  @Nonnull
  @Override
  public String toString() {
    return "%s - %s".formatted(code, name);
  }
}
