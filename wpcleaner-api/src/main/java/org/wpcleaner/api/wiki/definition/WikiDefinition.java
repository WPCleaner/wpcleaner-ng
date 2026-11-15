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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

  private static final String PATH_SEPARATOR = "/";
  private static final String REST = "rest.php";

  public WikiDefinition {
    apiPath = withLeadingSeparator(apiPath);
    indexPath = withLeadingSeparator(indexPath);
    wikiPath = withLeadingSeparator(wikiPath);
  }

  private static String withLeadingSeparator(final String path) {
    return path.startsWith(PATH_SEPARATOR) ? path : PATH_SEPARATOR + path;
  }

  public String apiUrl() {
    return "https://%s%s".formatted(mainHost, apiPath);
  }

  public String restUrl() {
    return "https://%s%s%s"
        .formatted(mainHost, apiPath.substring(0, apiPath.lastIndexOf(PATH_SEPARATOR) + 1), REST);
  }

  public String pageUrl(final String title) {
    return "https://%s%s/%s".formatted(mainHost, wikiPath, title.replace(' ', '_'));
  }

  @Nullable
  public URI pageUri(final String title) {
    try {
      return new URI("https", mainHost, wikiPath + "/" + title.replace(' ', '_'), null);
    } catch (final URISyntaxException _) {
      return null;
    }
  }

  public URI historyUri(final String title) {
    return URI.create(
        "https://%s%s?title=%s&action=history"
            .formatted(
                mainHost,
                indexPath,
                URLEncoder.encode(title.replace(' ', '_'), StandardCharsets.UTF_8)));
  }

  @Override
  public String toString() {
    return "%s - %s".formatted(code, name);
  }
}
