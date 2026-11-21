package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a003;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.title.TitleBuilder;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

record ReferencesInsertion(
    int algorithm,
    String insert,
    List<String> titles,
    List<String> beforeTitles,
    Set<PageTitle> beforeTemplates) {

  private static final String DEFAULT_SORT = "defaultsort";
  private static final int DEFAULT_TITLE_LEVEL = 2;
  private static final char LINE_END = '\n';
  private static final char SPACE = ' ';
  private static final char CLOSING_BRACE = '}';
  private static final String PARAGRAPH_END = "\n\n";

  ReferencesInsertion {
    titles = List.copyOf(titles);
    beforeTitles = List.copyOf(beforeTitles);
    beforeTemplates = Set.copyOf(beforeTemplates);
  }

  List<CheckError> suggest(final PageAnalysis analysis, final Element lastReference) {
    final List<Suggestion> suggestions = new ArrayList<>();
    final Set<String> possibleTitles = toUpperCase(titles);
    analysis.getTitles().stream()
        .filter(title -> possibleTitles.contains(title.title().toUpperCase(Locale.ROOT)))
        .forEach(
            title ->
                suggestions.add(
                    new Suggestion(
                        title,
                        analysis.getText().substring(title.begin(), title.end())
                            + LINE_END
                            + insert,
                        lastReference.end() <= title.begin())));
    final Set<String> possibleBeforeTitles = toUpperCase(beforeTitles);
    analysis.getTitles().stream()
        .filter(title -> possibleBeforeTitles.contains(title.title().toUpperCase(Locale.ROOT)))
        .forEach(
            title ->
                suggestions.add(
                    suggestBefore(
                        analysis, title, title.level(), lastReference.end() <= title.begin())));
    analysis.getTemplates().stream()
        .filter(template -> beforeTemplates.contains(template.title()))
        .findFirst()
        .ifPresent(
            template ->
                suggestions.add(
                    suggestBefore(
                        analysis,
                        template,
                        DEFAULT_TITLE_LEVEL,
                        lastReference.end() <= template.begin()
                            && !isAfterTemplate(analysis.getText(), template.begin()))));
    if (suggestions.isEmpty()) {
      findFirstCategory(analysis)
          .ifPresent(
              element ->
                  suggestions.add(suggestBefore(analysis, element, DEFAULT_TITLE_LEVEL, false)));
    }
    final boolean automaticAllowed =
        analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id;
    return IntStream.range(0, suggestions.size())
        .mapToObj(
            index -> suggestions.get(index).toError(algorithm, index == 0 && automaticAllowed))
        .toList();
  }

  private Suggestion suggestBefore(
      final PageAnalysis analysis,
      final Element element,
      final int titleLevel,
      final boolean automatic) {
    final String text = analysis.getText();
    final boolean lineEndBefore =
        element.begin() == 0 || text.charAt(element.begin() - 1) == LINE_END;
    final String title =
        titles.isEmpty() ? "" : TitleBuilder.from(titleLevel, titles.getFirst()).build() + LINE_END;
    return new Suggestion(
        element,
        (lineEndBefore ? "" : PARAGRAPH_END)
            + title
            + insert
            + PARAGRAPH_END
            + text.substring(element.begin(), element.end()),
        automatic);
  }

  private static boolean isAfterTemplate(final String text, final int begin) {
    int index = begin - 1;
    while (index >= 0 && text.charAt(index) == SPACE) {
      index--;
    }
    return index >= 0 && text.charAt(index) == CLOSING_BRACE;
  }

  private static Optional<Element> findFirstCategory(final PageAnalysis analysis) {
    return analysis.getCategories().stream()
        .findFirst()
        .map(
            category ->
                analysis.getFunctions().stream()
                    .filter(function -> DEFAULT_SORT.equals(function.magicWord()))
                    .findFirst()
                    .filter(defaultSort -> defaultSort.begin() < category.begin())
                    .<Element>map(defaultSort -> defaultSort)
                    .orElse(category));
  }

  private static Set<String> toUpperCase(final List<String> values) {
    return values.stream()
        .map(value -> value.trim().toUpperCase(Locale.ROOT))
        .collect(Collectors.toSet());
  }

  private record Suggestion(Element element, String replacement, boolean automatic) {

    CheckError toError(final int algorithm, final boolean firstAutomatic) {
      return new CheckError(
          algorithm,
          element.begin(),
          element.end(),
          List.of(new Replacement(replacement, automatic && firstAutomatic)));
    }
  }
}
