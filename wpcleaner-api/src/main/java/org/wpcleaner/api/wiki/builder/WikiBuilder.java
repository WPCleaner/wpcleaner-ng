package org.wpcleaner.api.wiki.builder;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.ComponentOrientation;
import java.util.HashSet;
import java.util.Set;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikiGroup;
import org.wpcleaner.api.wiki.definition.WikiWarning;
import org.wpcleaner.lib.image.ImageCollection;

public class WikiBuilder {

  private final String language;
  private final String name;
  private final ComponentOrientation orientation;
  private ImageCollection icon;
  private WikiGroup group;
  private final Set<String> hosts;
  private String apiPath;
  private String indexPath;
  private String code;
  @Nullable private String checkWikiCode;
  @Nullable private WikiWarning warning;

  public static WikiBuilder ltr(final String language, final String name) {
    return new WikiBuilder(language, name, ComponentOrientation.LEFT_TO_RIGHT);
  }

  public static WikiBuilder rtl(final String language, final String name) {
    return new WikiBuilder(language, name, ComponentOrientation.RIGHT_TO_LEFT);
  }

  WikiBuilder(final String language, final String name, final ComponentOrientation orientation) {
    this.language = language;
    this.name = name;
    this.orientation = orientation;
    this.icon = ImageCollection.LOGO_MEDIAWIKI;
    this.group = WikiGroup.OTHER;
    this.hosts = new HashSet<>();
    this.apiPath = "/w/api.php";
    this.indexPath = "/w/index.php";
    this.code = "other:" + language;
  }

  public WikiBuilder withIcon(final ImageCollection icon) {
    this.icon = icon;
    return this;
  }

  public WikiBuilder withGroup(final WikiGroup group) {
    this.group = group;
    return this;
  }

  public WikiBuilder withHost(final String host) {
    this.hosts.add(host);
    return this;
  }

  public WikiBuilder withApiPath(final String path) {
    this.apiPath = path;
    return this;
  }

  public WikiBuilder withIndexPath(final String path) {
    this.indexPath = path;
    return this;
  }

  public WikiBuilder withCode(final String code) {
    this.code = code;
    return this;
  }

  public WikiBuilder withCheckWikiCode(final String code) {
    this.checkWikiCode = code;
    return this;
  }

  public WikiBuilder withWarning(@Nullable final WikiWarning warning) {
    this.warning = warning;
    return this;
  }

  public WikiDefinition build() {
    return new WikiDefinition(
        language,
        name,
        icon,
        group,
        hosts,
        apiPath,
        indexPath,
        code,
        checkWikiCode,
        orientation,
        warning);
  }
}
