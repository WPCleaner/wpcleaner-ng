package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm067 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 67;
  private static final String PARAMETER_ABBREVIATIONS = "abbreviations";
  private static final String PARAMETER_SAFE_TEMPLATES = "safe_templates";
  private static final String PARAMETER_SEPARATOR = "separator";
  private static final String PUNCTUATION = ".,;:!?";
  private static final String GROUPING_PUNCTUATION = ",;.'";
  private static final String PERIOD = ".";
  private static final String ELLIPSIS = "...";
  private static final String COMMA = ",";
  private static final String COLON = ":";
  private static final char SEMICOLON = ';';
  private static final char SPACE = ' ';
  private static final char NEW_LINE = '\n';
  private static final Pattern ENTITY =
      Pattern.compile("&(?:[A-Za-z][A-Za-z0-9]*|#[0-9]+|#[xX][0-9A-Fa-f]+)$");

  private final CheckWikiConfigurationRepository checkWikiRepository;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckAlgorithm067(
      final CheckWikiConfigurationRepository checkWikiRepository,
      final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.checkWikiRepository = checkWikiRepository;
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final String separator = configuration.getString(PARAMETER_SEPARATOR).orElse("");
    final Map<PageTitle, Set<String>> safeTemplates = new HashMap<>();
    configuration
        .getTemplateParameters(PARAMETER_SAFE_TEMPLATES, PARAMETER_SAFE_TEMPLATES)
        .parameters()
        .forEach(
            (name, parameters) ->
                analysis
                    .normalizeTitle(name, CommonNamespaces.TEMPLATE.id)
                    .ifPresent(title -> safeTemplates.put(title, parameters)));
    final int namespace = analysis.getPageTitle().namespace();
    final Context context =
        new Context(
            analysis,
            separator,
            configuration.getStrings(PARAMETER_ABBREVIATIONS).stream()
                .map(String::trim)
                .filter(abbreviation -> !abbreviation.isEmpty())
                .toList(),
            GeneralAbbreviation.of(
                wpcleanerRepository
                    .getConfiguration(analysis.getWiki())
                    .getLines(WPCleanerParameter.ABBREVIATIONS)),
            Map.copyOf(safeTemplates),
            namespace % 2 == 0 && namespace != CommonNamespaces.USER.id);
    final ReferenceGrouper grouper =
        new ReferenceGrouper(GROUPING_PUNCTUATION, separator, true, Set.of());
    return grouper.group(analysis, ReferenceGrouper.findReferences(analysis, Set.of())).stream()
        .flatMap(group -> analyzeGroup(context, group).stream())
        .toList();
  }

  private static Optional<CheckError> analyzeGroup(
      final Context context, final List<Element> group) {
    final String text = context.analysis().getText();
    final String references =
        group.stream()
            .map(reference -> text.substring(reference.begin(), reference.end()))
            .collect(Collectors.joining(context.separator()));
    final int punctuationEnd =
        skipWhitespaceAndCommentsBefore(context.analysis(), group.getFirst().begin());
    if (isPunctuationBefore(text, punctuationEnd) && !isAbbreviation(context, punctuationEnd)) {
      return Optional.of(createError(context, group, punctuationEnd, references));
    }
    final boolean punctuationBetween =
        group.stream()
            .skip(1)
            .anyMatch(
                reference ->
                    isPunctuationBefore(
                        text,
                        skipWhitespaceAndCommentsBefore(context.analysis(), reference.begin())));
    return punctuationBetween
        ? Optional.of(
            new CheckError(
                ALGORITHM_NUMBER,
                group.getFirst().begin(),
                group.getLast().end(),
                List.of(new Replacement(references, false))))
        : Optional.empty();
  }

  private static CheckError createError(
      final Context context,
      final List<Element> group,
      final int punctuationEnd,
      final String references) {
    final String text = context.analysis().getText();
    final int punctuationBegin =
        skipBefore(text, punctuationEnd - 1, text.charAt(punctuationEnd - 1));
    final int begin = skipBefore(text, punctuationBegin, SPACE);
    final String spaces = text.substring(begin, punctuationBegin);
    final String allPunctuation = text.substring(punctuationBegin, punctuationEnd);
    final String movedSpaces = allPunctuation.startsWith(PERIOD) ? "" : spaces;
    final String comments =
        joinComments(context.analysis(), punctuationEnd, group.getFirst().begin());
    final int afterReferences = skipSpacesAfter(text, group.getLast().end());
    final int punctuationAfterEnd = skipPunctuationAfter(text, afterReferences);
    final String punctuationAfter = text.substring(afterReferences, punctuationAfterEnd);
    final int end = punctuationAfter.isEmpty() ? group.getLast().end() : punctuationAfterEnd;
    final boolean automatic =
        context.automaticAllowed()
            && isAutomatic(
                allPunctuation,
                punctuationAfter,
                FollowingText.at(context.analysis(), end, context.safeTemplates()));
    final List<GeneralAbbreviation> generalAbbreviations =
        context.generalAbbreviations().stream()
            .filter(abbreviation -> endsWithWord(text, punctuationEnd, abbreviation.text()))
            .toList();
    final int errorBegin =
        generalAbbreviations.stream()
            .mapToInt(abbreviation -> punctuationEnd - abbreviation.text().length())
            .reduce(begin, Math::min);
    final String kept = text.substring(errorBegin, begin) + comments + references;
    final List<Replacement> replacements = new ArrayList<>();
    generalAbbreviations.forEach(
        abbreviation ->
            replacements.add(
                new Replacement(
                    abbreviation.replacement() + comments + references + punctuationAfter, false)));
    replacements.add(new Replacement(kept + movedSpaces + allPunctuation, automatic));
    if (!punctuationAfter.isEmpty() && !punctuationAfter.equals(allPunctuation)) {
      replacements.add(new Replacement(kept + spaces + punctuationAfter, false));
    }
    return new CheckError(ALGORITHM_NUMBER, errorBegin, end, replacements);
  }

  private static String joinComments(final PageAnalysis analysis, final int from, final int to) {
    final String text = analysis.getText();
    return analysis.getComments().stream()
        .filter(comment -> comment.begin() >= from && comment.end() <= to)
        .map(comment -> text.substring(comment.begin(), comment.end()))
        .collect(Collectors.joining());
  }

  private static int skipBefore(final String text, final int index, final char character) {
    int current = index;
    while (current > 0 && text.charAt(current - 1) == character) {
      current--;
    }
    return current;
  }

  private static int skipPunctuationAfter(final String text, final int index) {
    int current = index;
    while (current < text.length() && PUNCTUATION.indexOf(text.charAt(current)) >= 0) {
      current++;
    }
    return current;
  }

  private static boolean isAutomatic(
      final String allPunctuation, final String punctuationAfter, final FollowingText following) {
    if (punctuationAfter.isEmpty()) {
      return switch (allPunctuation) {
        case PERIOD ->
            following == FollowingText.END_OF_LINE
                || following == FollowingText.UPPERCASE
                || following == FollowingText.SAFE_TEMPLATE_PARAMETER;
        case ELLIPSIS, COLON -> following == FollowingText.END_OF_LINE;
        case COMMA -> following == FollowingText.LOWERCASE;
        default -> false;
      };
    }
    if (!allPunctuation.equals(punctuationAfter)) {
      return false;
    }
    return COMMA.equals(allPunctuation)
        || COLON.equals(allPunctuation)
        || (PERIOD.equals(allPunctuation)
            && (following == FollowingText.END_OF_LINE || following == FollowingText.UPPERCASE));
  }

  private static boolean isPunctuationBefore(final String text, final int index) {
    if (index <= 0 || PUNCTUATION.indexOf(text.charAt(index - 1)) < 0) {
      return false;
    }
    return text.charAt(index - 1) != SEMICOLON
        || (!ENTITY.matcher(text.substring(0, index - 1)).find()
            && (index < 2 || text.charAt(index - 2) != NEW_LINE));
  }

  private static boolean isAbbreviation(final Context context, final int index) {
    final String text = context.analysis().getText();
    return context.abbreviations().stream()
        .anyMatch(abbreviation -> endsWithWord(text, index, abbreviation));
  }

  private static boolean endsWithWord(final String text, final int index, final String word) {
    final int begin = index - word.length();
    return begin >= 0
        && text.startsWith(word, begin)
        && (begin == 0 || !Character.isLetterOrDigit(text.charAt(begin - 1)));
  }

  private static int skipWhitespaceAndCommentsBefore(final PageAnalysis analysis, final int index) {
    final String text = analysis.getText();
    int current = index;
    boolean moved = true;
    while (moved) {
      moved = false;
      while (current > 0 && Character.isWhitespace(text.charAt(current - 1))) {
        current--;
      }
      final int commentEnd = current;
      final Optional<CommentElement> comment =
          analysis.getComments().stream()
              .filter(element -> element.end() == commentEnd)
              .findFirst();
      if (comment.isPresent()) {
        current = comment.get().begin();
        moved = true;
      }
    }
    return current;
  }

  private static int skipSpacesAfter(final String text, final int index) {
    int current = index;
    while (current < text.length() && text.charAt(current) == SPACE) {
      current++;
    }
    return current;
  }

  private record Context(
      PageAnalysis analysis,
      String separator,
      List<String> abbreviations,
      List<GeneralAbbreviation> generalAbbreviations,
      Map<PageTitle, Set<String>> safeTemplates,
      boolean automaticAllowed) {}
}
