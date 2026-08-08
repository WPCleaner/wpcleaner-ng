package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.awt.ComponentOrientation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.jspecify.annotations.Nullable;
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
    @JsonDeserialize(using = ComponentOrientationDeserializer.class)
        @JsonSerialize(using = ComponentOrientationSerializer.class)
        ComponentOrientation orientation,
    @Nullable WikiWarning warning) {

  public String apiUrl() {
    return "https://%s/%s".formatted(mainHost, apiPath);
  }

  public String pageUrl(final String title) {
    return "https://%s/%s/%s".formatted(mainHost, wikiPath, title.replace(' ', '_'));
  }

  @Nullable
  public URI pageUri(final String title) {
    try {
      return new URI("https", mainHost, wikiPath + "/" + title.replace(' ', '_'), null);
    } catch (final URISyntaxException _) {
      return null;
    }
  }

  @Override
  public String toString() {
    return "%s - %s".formatted(code, name);
  }
}
