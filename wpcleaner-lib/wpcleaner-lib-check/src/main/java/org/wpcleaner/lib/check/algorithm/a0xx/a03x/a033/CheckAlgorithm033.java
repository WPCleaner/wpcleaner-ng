package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a033;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.template.TemplateBuilder;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.UnwantedTags;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm033 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 33;
  private static final String PARAMETER_TEMPLATES = "templates";
  private static final UnwantedTags UNWANTED_TAGS = new UnwantedTags(Set.of(HtmlTagType.U));
  private static final String ITALIC = "''";
  private static final String BOLD = "'''";
  private static final String FIRST_PARAMETER = "1";
  private static final List<Replacement> DELETION = List.of(new Replacement("", false));

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm033(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final List<Element> tags = UNWANTED_TAGS.findComplete(analysis);
    if (tags.isEmpty()) {
      return List.of();
    }
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final List<String> templates = configuration.getStrings(PARAMETER_TEMPLATES);
    final String text = analysis.getText();
    return tags.stream()
        .map(
            tag ->
                new CheckError(
                    ALGORITHM_NUMBER, tag.begin(), tag.end(), replacements(text, tag, templates)))
        .toList();
  }

  private static List<Replacement> replacements(
      final String text, final Element tag, final List<String> templates) {
    if (tag instanceof final TagElement singleTag) {
      return singleTag.kind() == TagKind.SELF_CLOSING ? DELETION : List.of();
    }
    if (!(tag instanceof final CompleteTagElement completeTag)) {
      return List.of();
    }
    final String contents =
        text.substring(completeTag.startTag().end(), completeTag.endTag().begin());
    if (contents.isBlank()) {
      return DELETION;
    }
    final List<Replacement> replacements = new ArrayList<>();
    replacements.add(new Replacement(contents, false));
    templates.forEach(
        template ->
            replacements.add(
                new Replacement(
                    TemplateBuilder.from(template).withParameter(FIRST_PARAMETER, contents).build(),
                    false)));
    replacements.add(new Replacement(ITALIC + contents + ITALIC, false));
    replacements.add(new Replacement(BOLD + contents + BOLD, false));
    return List.copyOf(replacements);
  }
}
