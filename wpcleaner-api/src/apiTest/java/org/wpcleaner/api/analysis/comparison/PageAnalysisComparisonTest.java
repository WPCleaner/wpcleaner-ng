package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.parse.ApiParse;
import org.wpcleaner.api.api.parse.Parse;
import org.wpcleaner.api.api.parse.ParseParameters;
import org.wpcleaner.api.api.parse.ParseQuery;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.api.query.list.random.RandomPage;
import org.wpcleaner.api.api.query.list.random.RandomParameters;
import org.wpcleaner.api.api.query.list.random.RandomQuery;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.hook.login.LoginHook;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationLoader;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class PageAnalysisComparisonTest {

  private static final String PARSE_TITLE = "WPCleanerNG analysis comparison";
  private static final String MAIN_SLOT = "main";
  private static final String TEMPLATE_PLACEHOLDER = "x";
  private static final String REFERENCE_TAG = "ref";
  private static final String REFERENCE_GROUP_ATTRIBUTE = "group";
  private static final int RANDOM_PAGES = 7;
  private static final Path REPORT =
      Path.of("build", "reports", "page-analysis-comparison", "report.md");
  private static final Map<WikiDefinition, List<String>> FIXED_PAGES =
      Map.of(
          WikipediaDefinitions.FR,
          List.of("Paris", "Victor Hugo", "Tour Eiffel"),
          WikipediaDefinitions.EN,
          List.of("Paris", "Albert Einstein", "Eiffel Tower"));

  @Autowired private ApiParse apiParse;
  @Autowired private ApiRandom apiRandom;
  @Autowired private ApiRevisions apiRevisions;
  @Autowired private ExtensionTagRepository extensionTagRepository;
  @Autowired private LoginHook loginHook;
  @Autowired private PageAnalysisFactory pageAnalysisFactory;

  @DisplayName("Compare page analysis with MediaWiki on real pages")
  @Test
  void compareWithMediaWiki() throws IOException {
    // GIVEN
    final ComparisonReport report = new ComparisonReport();

    // WHEN
    for (final WikiDefinition wiki : List.of(WikipediaDefinitions.FR, WikipediaDefinitions.EN)) {
      loginHook.executeHook(
          wiki, WPCleanerConfigurationLoader.GENERAL_USERNAME, new ProgressTracker(_ -> {}));
      final Set<String> extensionTags =
          extensionTagRepository.getExtensionTags(wiki).stream()
              .map(ExtensionTag::name)
              .collect(Collectors.toSet());
      for (final Page page : retrievePages(wiki)) {
        findContent(page)
            .ifPresent(content -> comparePage(wiki, page, content, extensionTags, report));
      }
    }
    Files.createDirectories(REPORT.getParent());
    Files.writeString(REPORT, report.toMarkdown());

    // THEN
    Assertions.assertThat(report.getPageCount()).as("compared pages").isPositive();
    Assertions.assertThat(report.getReconstructionFailures())
        .as("parse trees not matching the page text")
        .isEmpty();
  }

  private void comparePage(
      final WikiDefinition wiki,
      final Page page,
      final String content,
      final Set<String> extensionTags,
      final ComparisonReport report) {
    final String title = Optional.ofNullable(page.title()).orElse("");
    final Parse treeParse =
        apiParse.parse(
            wiki,
            new ParseQuery(PARSE_TITLE, content, Set.of(ParseParameters.Properties.PARSE_TREE)));
    final ParseTree parseTree =
        ParseTree.read(Optional.ofNullable(treeParse.parseTree()).orElse("<root/>"));
    if (!parseTree.text().equals(content)) {
      report.addReconstructionFailure(wiki, title);
      return;
    }
    final PageComparator comparator = new PageComparator();
    final PageAnalysis analysis = pageAnalysisFactory.analysis(wiki, title, content);
    comparator.comparePreprocessor(analysis, parseTree, extensionTags);
    final String blankedContent = blankTemplates(content, parseTree, analysis);
    final Parse blankedParse =
        apiParse.parse(
            wiki,
            new ParseQuery(
                PARSE_TITLE,
                blankedContent,
                EnumSet.complementOf(
                    EnumSet.of(
                        ParseParameters.Properties.PARSE_TREE,
                        ParseParameters.Properties.TEMPLATES))));
    final PageAnalysis blankedAnalysis = pageAnalysisFactory.analysis(wiki, title, blankedContent);
    comparator.compareWikiElements(
        blankedAnalysis, blankedParse, images -> retrieveUsedImages(wiki, images));
    report.addPage(wiki, title, revisionId(page), comparator.getDifferences());
  }

  private Set<String> retrieveUsedImages(final WikiDefinition wiki, final Set<String> images) {
    return Set.copyOf(
        apiParse
            .parse(
                wiki,
                new ParseQuery(
                    PARSE_TITLE,
                    images.stream().map("[[File:%s]]"::formatted).collect(Collectors.joining("\n")),
                    Set.of(ParseParameters.Properties.IMAGES)))
            .images());
  }

  private List<Page> retrievePages(final WikiDefinition wiki) {
    final List<String> titles = new ArrayList<>(FIXED_PAGES.get(wiki));
    apiRandom
        .retrieveRandomPages(
            wiki,
            RandomQuery.emptyBuilder()
                .limit(Limit.of(RANDOM_PAGES))
                .namespace(Set.of(Namespace.MAIN))
                .filterRedirect(RandomParameters.FilterRedirect.NON_REDIRECTS)
                .build())
        .stream()
        .map(RandomPage::title)
        .forEach(titles::add);
    return apiRevisions.retrieveRevisionsByTitle(
        wiki,
        titles,
        RevisionsQuery.emptyBuilder()
            .properties(
                Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
            .slots(Set.of(MAIN_SLOT))
            .build());
  }

  private static Optional<String> findContent(final Page page) {
    return page.revisions().stream()
        .findFirst()
        .map(Revision::slots)
        .map(slots -> slots.get(MAIN_SLOT))
        .map(RevisionSlot::content);
  }

  private static int revisionId(final Page page) {
    return page.revisions().stream().findFirst().map(Revision::revId).orElse(0);
  }

  private static String blankTemplates(
      final String content, final ParseTree parseTree, final PageAnalysis analysis) {
    final StringBuilder blanked = new StringBuilder(content.length());
    int index = 0;
    for (final Range range :
        Stream.of(
                parseTree.elements(ParseTreeElement.Kind.TEMPLATE).stream()
                    .map(element -> new Range(element.begin(), element.end())),
                parseTree.elements(ParseTreeElement.Kind.TEMPLATE_ARGUMENT).stream()
                    .map(element -> new Range(element.begin(), element.end())),
                Stream.of(
                        analysis.getTemplates(), analysis.getFunctions(), analysis.getParameters())
                    .flatMap(List::stream)
                    .map(element -> new Range(element.begin(), element.end())))
            .flatMap(ranges -> ranges)
            .sorted(Comparator.comparingInt(Range::begin))
            .toList()) {
      if (range.begin() >= index) {
        blanked.append(content, index, range.begin()).append(TEMPLATE_PLACEHOLDER);
        index = range.end();
      } else if (range.end() > index) {
        index = range.end();
      }
    }
    blanked.append(content, index, content.length());
    analysis.getTags().stream()
        .filter(tag -> REFERENCE_TAG.equals(tag.normalizedName()))
        .flatMap(tag -> tag.attributes().stream())
        .filter(attribute -> REFERENCE_GROUP_ATTRIBUTE.equals(attribute.name()))
        .map(TagAttribute::value)
        .filter(Objects::nonNull)
        .distinct()
        .forEach(group -> blanked.append("\n<references group=\"").append(group).append("\"/>"));
    return blanked.toString();
  }

  private record Range(int begin, int end) {}
}
