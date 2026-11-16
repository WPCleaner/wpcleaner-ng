package org.wpcleaner.lib.check.algorithm.a5xx.a56x.a566;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.UnwantedTags;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm566 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 566;
  private static final UnwantedTags UNWANTED_TAGS = new UnwantedTags(Set.of(HtmlTagType.ABBR));
  private static final String ATTRIBUTE_TITLE = "title";
  private static final Set<String> AUTOMATIC_ATTRIBUTES = Set.of(ATTRIBUTE_TITLE, "class");
  private static final Pattern SURROUNDING_SPACES =
      Pattern.compile("^((?:&nbsp;| )*)(.*?)((?:&nbsp;| )*)$", Pattern.DOTALL);
  private static final int PREFIX_GROUP = 1;
  private static final int ABBREVIATION_GROUP = 2;
  private static final int SUFFIX_GROUP = 3;

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm566(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
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
    final Context context =
        new Context(
            analysis.getText(),
            ReplacementConfiguration.of(configuration),
            TemplateConfiguration.of(configuration),
            analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id);
    return UNWANTED_TAGS.findComplete(analysis).stream()
        .map(tag -> createError(context, tag))
        .toList();
  }

  private static CheckError createError(final Context context, final Element tag) {
    final String text = context.text();
    final int begin = findWordBegin(text, tag.begin());
    final int end = findWordEnd(text, tag.end());
    final String prefix = text.substring(begin, tag.begin());
    final String suffix = text.substring(tag.end(), end);
    return new CheckError(
        ALGORITHM_NUMBER,
        begin,
        end,
        tag instanceof CompleteTagElement completeTag
            ? createReplacements(context, completeTag, prefix, suffix)
            : List.of(new Replacement(prefix + suffix, false)));
  }

  private static List<Replacement> createReplacements(
      final Context context,
      final CompleteTagElement tag,
      final String prefix,
      final String suffix) {
    final Matcher contents =
        SURROUNDING_SPACES.matcher(
            context.text().substring(tag.startTag().end(), tag.endTag().begin()));
    if (!contents.matches()) {
      return List.of();
    }
    final String fullPrefix = prefix + contents.group(PREFIX_GROUP);
    final String abbreviation = contents.group(ABBREVIATION_GROUP);
    final String fullSuffix = contents.group(SUFFIX_GROUP) + suffix;
    final String title = findTitle(tag.startTag());
    final boolean automaticAllowed =
        context.automaticAllowed()
            && tag.startTag().attributes().stream()
                .allMatch(attribute -> AUTOMATIC_ATTRIBUTES.contains(attribute.name()));
    final Map<String, Replacement> replacements = new LinkedHashMap<>();
    context
        .replacementConfiguration()
        .getReplacement(abbreviation, title)
        .ifPresent(
            replacement ->
                replacements.putIfAbsent(
                    replacement.text(),
                    new Replacement(
                        fullPrefix + replacement.text() + fullSuffix,
                        replacement.automatic() && automaticAllowed)));
    context
        .templates()
        .forEach(
            template -> {
              final String replacement = template.getReplacement(abbreviation, title);
              replacements.putIfAbsent(
                  replacement, new Replacement(fullPrefix + replacement + fullSuffix, false));
            });
    replacements.putIfAbsent(
        abbreviation, new Replacement(fullPrefix + abbreviation + fullSuffix, false));
    return List.copyOf(replacements.values());
  }

  private static @Nullable String findTitle(final TagElement tag) {
    return tag.attributes().stream()
        .filter(attribute -> ATTRIBUTE_TITLE.equals(attribute.name()))
        .map(TagAttribute::value)
        .findFirst()
        .orElse(null);
  }

  private static int findWordBegin(final String text, final int from) {
    int index = from;
    while (index > 0 && isWordCharacter(text.charAt(index - 1))) {
      index--;
    }
    return index;
  }

  private static int findWordEnd(final String text, final int from) {
    int index = from;
    while (index < text.length() && isWordCharacter(text.charAt(index))) {
      index++;
    }
    return index;
  }

  private static boolean isWordCharacter(final char character) {
    return Character.isDigit(character)
        || (Character.isLetter(character) && isClassicLetter(character));
  }

  private static boolean isClassicLetter(final char character) {
    return character <= 'z' || (character >= 'À' && character <= 'ɏ' && character != 'ŉ');
  }

  private record Context(
      String text,
      ReplacementConfiguration replacementConfiguration,
      List<TemplateConfiguration> templates,
      boolean automaticAllowed) {}
}
