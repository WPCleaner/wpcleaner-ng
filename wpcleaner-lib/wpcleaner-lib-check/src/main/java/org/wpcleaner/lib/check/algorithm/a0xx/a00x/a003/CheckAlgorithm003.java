package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a003;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.ReferenceGrouper;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm003 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 3;
  private static final String PARAMETER_BEFORE_TEMPLATES = "before_templates";
  private static final String PARAMETER_BEFORE_TITLES = "before_titles";
  private static final String PARAMETER_INSERT = "insert";
  private static final String PARAMETER_REF_TEMPLATES = "ref_templates";
  private static final String PARAMETER_REFERENCES_TEMPLATES = "references_templates";
  private static final String PARAMETER_TEMPLATES = "templates";
  private static final String PARAMETER_TITLES = "titles";
  private static final String REFERENCES_TAG = "<references />";
  private static final String INCORRECT_END_TAG = "</references/>";
  private static final String END_TAG = "</references>";
  private static final char TAG_START = '<';

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm003(final CheckWikiConfigurationRepository checkWikiRepository) {
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
    final List<Element> references =
        ReferenceGrouper.findReferences(
            analysis, findTemplates(analysis, configuration.getStrings(PARAMETER_REF_TEMPLATES)));
    if (references.isEmpty()
        || hasReferencesTag(analysis)
        || hasReferencesTemplate(analysis, configuration)) {
      return List.of();
    }
    final Element lastReference = references.getLast();
    final List<CheckError> errors = new ArrayList<>();
    errors.add(
        new CheckError(ALGORITHM_NUMBER, lastReference.begin(), lastReference.end(), List.of()));
    final List<TagElement> referencesTags = findReferencesTags(analysis);
    if (referencesTags.isEmpty()) {
      configuration
          .getString(PARAMETER_INSERT)
          .map(String::trim)
          .filter(insert -> !insert.isEmpty())
          .ifPresent(
              insert ->
                  errors.addAll(
                      new ReferencesInsertion(
                              ALGORITHM_NUMBER,
                              insert,
                              configuration.getStrings(PARAMETER_TITLES),
                              configuration.getStrings(PARAMETER_BEFORE_TITLES),
                              findTemplates(
                                  analysis, configuration.getStrings(PARAMETER_BEFORE_TEMPLATES)))
                          .suggest(analysis, lastReference)));
      return List.copyOf(errors);
    }
    referencesTags.forEach(
        tag ->
            errors.add(
                new CheckError(
                    ALGORITHM_NUMBER,
                    tag.begin(),
                    tag.end(),
                    referencesTags.size() == 1
                        ? List.of(new Replacement(REFERENCES_TAG, false))
                        : List.of())));
    if (referencesTags.size() == 1) {
      findIncorrectEndTag(analysis, referencesTags.getFirst(), references).ifPresent(errors::add);
    }
    return List.copyOf(errors);
  }

  private static Optional<CheckError> findIncorrectEndTag(
      final PageAnalysis analysis, final TagElement referencesTag, final List<Element> references) {
    final String text = analysis.getText();
    int index = referencesTag.end();
    while (index < text.length()) {
      if (Character.isWhitespace(text.charAt(index))) {
        index++;
      } else if (text.charAt(index) == TAG_START) {
        final int currentIndex = index;
        final Optional<Element> reference =
            references.stream().filter(element -> element.begin() == currentIndex).findFirst();
        if (reference.isEmpty()) {
          return text.startsWith(INCORRECT_END_TAG, index)
              ? Optional.of(
                  new CheckError(
                      ALGORITHM_NUMBER,
                      index,
                      index + INCORRECT_END_TAG.length(),
                      List.of(new Replacement(END_TAG, true))))
              : Optional.empty();
        }
        index = reference.get().end();
      } else {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private static boolean hasReferencesTag(final PageAnalysis analysis) {
    final String references = WikiTagType.REFERENCES.getNormalizedName();
    return analysis.getCompleteTags().stream()
            .anyMatch(tag -> references.equals(tag.startTag().normalizedName()))
        || analysis.getTags().stream()
            .filter(tag -> tag.kind() == TagKind.SELF_CLOSING)
            .anyMatch(tag -> references.equals(tag.normalizedName()));
  }

  private static List<TagElement> findReferencesTags(final PageAnalysis analysis) {
    final String references = WikiTagType.REFERENCES.getNormalizedName();
    return analysis.getTags().stream()
        .filter(tag -> references.equals(tag.normalizedName()))
        .filter(tag -> !analysis.getText().startsWith(INCORRECT_END_TAG, tag.begin()))
        .toList();
  }

  private static boolean hasReferencesTemplate(
      final PageAnalysis analysis, final CheckAlgorithmConfiguration configuration) {
    final List<String> templates = configuration.getUnsplitStrings(PARAMETER_TEMPLATES);
    return ReferencesTemplates.of(
            analysis,
            templates.isEmpty()
                ? configuration.getUnsplitStrings(PARAMETER_REFERENCES_TEMPLATES)
                : templates)
        .isUsedIn(analysis);
  }

  private static Set<PageTitle> findTemplates(
      final PageAnalysis analysis, final List<String> names) {
    return names.stream()
        .flatMap(
            name -> analysis.normalizeTitle(name.trim(), CommonNamespaces.TEMPLATE.id).stream())
        .collect(Collectors.toSet());
  }
}
