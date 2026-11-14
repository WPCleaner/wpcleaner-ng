package org.wpcleaner.application.bot;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.application.base.ApplicationLogin;
import org.wpcleaner.application.bot.content.ContentFileWriter;
import org.wpcleaner.application.bot.content.ContentFilesLocator;
import org.wpcleaner.application.bot.content.ContentPage;
import org.wpcleaner.application.bot.task.contentcurrent.CurrentRevisionChecker;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationLoader;

@BotSpringBootTest
class BotRunnerTest {

  private static final String DATE = "2026-11-01";
  private static final String CHECK_WIKI_CONFIGURATION = "error_048_prio_frwiki=0 END";

  @Autowired private ApplicationLogin applicationLogin;
  @Autowired private BotRunner botRunner;
  @Autowired private CheckWikiConfigurationLoader checkWikiConfigurationLoader;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @TempDir private Path folder;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
    mediaWikiServer.reset();
  }

  @DisplayName("should report the errors still present in the current revisions of the pages")
  @Test
  void processContentCurrent() throws IOException {
    // GIVEN
    givenContentFiles(
        List.of(
            new ContentPage(1, 0, "Paris", "wikitext", "Capitale : [[Paris]] & <b>[[Paris]]</b>"),
            new ContentPage(2, 0, "Lyon", "wikitext", "Ville : [[Lyon]]"),
            new ContentPage(3, 0, "Marseille", "wikitext", "Ville : [[Nice]]"),
            new ContentPage(4, 2, "Utilisateur:Bot", "wikitext", "[[Utilisateur:Bot]]"),
            new ContentPage(5, 0, "Nice", "wikitext", "Ville :\n[[Nice]]")));
    final Path script =
        givenScript(
            """
            tasks:
              - type: processContentCurrent
                report: [48]
            """);
    givenLogin();
    mockApiRevisions
        .givenRetrieveRevisionsByPageId(
            BotCredentialsReader.WIKI,
            List.of(1, 2, 5),
            CurrentRevisionChecker.CURRENT_CONTENT_QUERY)
        .willReturn(
            List.of(
                createPage(1, "Paris (France)", "Capitale : [[Paris (France)]] & <b>"),
                createPage(2, "Lyon", "Ville : Lyon"),
                createPage(5, "Nice", "Ville :\n[[Nice]] et [[Nice|ici]]")));
    final Path output = folder.resolve("output");

    // WHEN
    final int exitCode = runBot(script, output);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(exitCode).isEqualTo(BotRunner.SUCCESS);
    Assertions.assertThat(output.resolve("frwiki-%s-errors.jsonl".formatted(DATE)))
        .content(StandardCharsets.UTF_8)
        .contains("\"pageId\":1,\"namespace\":0,\"title\":\"Paris (France)\"")
        .contains("\"pageId\":5")
        .doesNotContain("\"pageId\":2");
    Assertions.assertThat(output.resolve("frwiki-%s-error-048.txt".formatted(DATE)))
        .content(StandardCharsets.UTF_8)
        .isEqualTo(
            """
            <!-- Generated using frwiki-%s -->
            * [[Nice]]: <nowiki>[[Nice]]</nowiki>, <nowiki>[[Nice|ici]]</nowiki>
            * [[Paris (France)]]: <nowiki>[[Paris (France)]]</nowiki>
            """
                .formatted(DATE));
  }

  @DisplayName("should refuse to run without the required arguments")
  @Test
  void runWithoutArguments() {
    // WHEN
    final int exitCode = botRunner.run("--wiki=fr");

    // THEN
    Assertions.assertThat(exitCode).isEqualTo(BotRunner.INVALID_ARGUMENTS);
  }

  @DisplayName("should refuse to run a script with unknown algorithms")
  @Test
  void runWithUnknownAlgorithm() throws IOException {
    // GIVEN
    final Path script =
        givenScript(
            """
            tasks:
              - type: processContentCurrent
                report: [0]
            """);

    // WHEN
    final int exitCode = runBot(script, folder.resolve("output"));

    // THEN
    Assertions.assertThat(exitCode).isEqualTo(BotRunner.INVALID_ARGUMENTS);
  }

  @DisplayName("should fail when the content files are missing")
  @Test
  void runWithoutContentFiles() throws IOException {
    // GIVEN
    final Path script =
        givenScript(
            """
            tasks:
              - type: processContentCurrent
                report: [48]
            """);
    givenLogin();

    // WHEN
    final int exitCode = runBot(script, folder.resolve("output"));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(exitCode).isEqualTo(BotRunner.FAILURE);
  }

  private int runBot(final Path script, final Path output) {
    return botRunner.run(
        "--wiki=" + BotCredentialsReader.WIKI.code(),
        "--script=" + script,
        "--content-folder=" + folder.resolve("content"),
        "--output-folder=" + output);
  }

  private void givenLogin() {
    checkWikiConfigurationLoader.addAlgorithms(Set.of(48));
    try {
      applicationLogin.givenLogin(
          BotCredentialsReader.WIKI,
          BotCredentialsReader.USERNAME,
          new ApplicationLogin.LoginData(
                  ApplicationLogin.USER_NAMESPACES, List.of(), List.of("bot"), List.of())
              .withCheckWikiConfiguration(CHECK_WIKI_CONFIGURATION));
    } finally {
      checkWikiConfigurationLoader.removeAlgorithms(Set.of(48));
    }
  }

  private void givenContentFiles(final List<ContentPage> pages) throws IOException {
    final Path dateFolder =
        Files.createDirectories(folder.resolve("content").resolve("frwiki").resolve(DATE));
    final String name = "frwiki-%s-p1p5.xml.bz2".formatted(DATE);
    ContentFileWriter.write(dateFolder.resolve(name), pages);
    Files.writeString(
        dateFolder.resolve(ContentFilesLocator.CHECKSUMS_FILE),
        "%s  %s\n".formatted("0".repeat(64), name),
        StandardCharsets.UTF_8);
  }

  private Path givenScript(final String content) throws IOException {
    final Path script = folder.resolve("script.yaml");
    Files.writeString(script, content, StandardCharsets.UTF_8);
    return script;
  }

  private static Page createPage(final int pageId, final String title, final String content) {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            pageId * 1000,
            null,
            null,
            Map.of(Slot.MAIN.value, new RevisionSlot(content, "wikitext", null, null)),
            List.of(),
            null,
            null,
            null);
    return new Page(false, 0, pageId, List.of(revision), title);
  }
}
