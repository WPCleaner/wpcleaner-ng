package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class WPCleanerConfigurationExpectations {

  private WPCleanerConfigurationExpectations() {}

  public static void givenConfiguration(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final String username,
      @Nullable final String generalContent,
      @Nullable final String userContent) {
    givenConfigurationPage(
        mockApiRevisions, wiki, WPCleanerConfigurationLoader.GENERAL_USERNAME, generalContent);
    if (!WPCleanerConfigurationLoader.GENERAL_USERNAME.equals(username)) {
      givenConfigurationPage(mockApiRevisions, wiki, username, userContent);
    }
  }

  public static void givenNoConfiguration(
      final MockApiRevisions mockApiRevisions, final WikiDefinition wiki, final String username) {
    givenConfiguration(mockApiRevisions, wiki, username, null, null);
  }

  private static void givenConfigurationPage(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final String username,
      @Nullable final String content) {
    givenPage(
        mockApiRevisions,
        wiki,
        WPCleanerConfigurationLoader.getConfigurationPage(username),
        content);
  }

  public static void givenPage(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final String title,
      @Nullable final String content) {
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(wiki, List.of(title), ConfigurationPageReader.CONTENT_QUERY)
        .willReturn(List.of(createPage(title, content)));
  }

  public static void givenPages(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final List<String> titles,
      final Map<String, String> contentByTitle) {
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(wiki, titles, ConfigurationPageReader.CONTENT_QUERY)
        .willReturn(
            titles.stream().map(title -> createPage(title, contentByTitle.get(title))).toList());
  }

  public static Page createPage(final String title, @Nullable final String content) {
    if (content == null) {
      return new Page(true, 2, null, List.of(), title);
    }
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of(Slot.MAIN.value, new RevisionSlot(content, null, null, null)),
            List.of(),
            null,
            null,
            null);
    return new Page(false, 2, 1234, List.of(revision), title);
  }
}
