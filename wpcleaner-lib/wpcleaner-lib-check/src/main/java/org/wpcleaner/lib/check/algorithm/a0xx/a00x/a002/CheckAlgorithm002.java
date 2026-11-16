package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.algorithm.TextExclusions;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm002 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 2;
  private static final String PARAMETER_ANCHOR_TEMPLATES = "anchor_templates";
  private static final String PARAMETER_CLEAR_ALL = "clear_all";
  private static final String PARAMETER_CLEAR_LEFT = "clear_left";
  private static final String PARAMETER_CLEAR_RIGHT = "clear_right";
  private static final String FIELD_PARAMETER = "parameter";
  private static final String DEFAULT_PARAMETER = "1";

  private final CheckWikiConfigurationRepository checkWikiRepository;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckAlgorithm002(
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
    final WPCleanerConfiguration wpcleanerConfiguration =
        wpcleanerRepository.getConfiguration(analysis.getWiki());
    final ClearReplacements clearReplacements =
        new ClearReplacements(
            configuration.getString(PARAMETER_CLEAR_ALL).orElse(null),
            configuration.getString(PARAMETER_CLEAR_LEFT).orElse(null),
            configuration.getString(PARAMETER_CLEAR_RIGHT).orElse(null));
    final TextExclusions exclusions = TextExclusions.of(analysis);
    final List<CheckError> incorrectVoidTags =
        new IncorrectVoidTags(analysis, exclusions, getVoidTagKind(wpcleanerConfiguration))
            .analyze(ALGORITHM_NUMBER);
    return Stream.of(
            incorrectVoidTags.stream(),
            new VoidTagAttributes(analysis, clearReplacements)
                .analyze(ALGORITHM_NUMBER).stream()
                    .filter(error -> !overlaps(error, incorrectVoidTags)),
            new SelfClosedTags(
                    analysis,
                    clearReplacements,
                    getAnchorTemplates(configuration, wpcleanerConfiguration))
                .analyze(ALGORITHM_NUMBER).stream(),
            new IncorrectEndTags(analysis, exclusions).analyze(ALGORITHM_NUMBER).stream())
        .flatMap(errors -> errors)
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private static TagKind getVoidTagKind(final WPCleanerConfiguration configuration) {
    return configuration
            .getString(WPCleanerParameter.CLOSE_SELF_CLOSING_TAGS)
            .map(Boolean::parseBoolean)
            .orElse(true)
        ? TagKind.SELF_CLOSING
        : TagKind.START;
  }

  private static List<AnchorTemplate> getAnchorTemplates(
      final CheckAlgorithmConfiguration configuration,
      final WPCleanerConfiguration wpcleanerConfiguration) {
    final List<AnchorTemplate> templates =
        configuration
            .getTemplateDefinitions(PARAMETER_ANCHOR_TEMPLATES, List.of(FIELD_PARAMETER))
            .stream()
            .map(
                definition ->
                    new AnchorTemplate(
                        definition.template(),
                        definition.parameters().getOrDefault(FIELD_PARAMETER, DEFAULT_PARAMETER)))
            .toList();
    if (!templates.isEmpty()) {
      return templates;
    }
    return wpcleanerConfiguration.getLines(WPCleanerParameter.ANCHOR_TEMPLATES).stream()
        .map(ConfigurationParser::splitLine)
        .filter(line -> !line.getFirst().isBlank())
        .map(
            line ->
                new AnchorTemplate(
                    line.getFirst().trim(),
                    line.size() > 1 && !line.get(1).isBlank()
                        ? line.get(1).trim()
                        : DEFAULT_PARAMETER))
        .toList();
  }

  private static boolean overlaps(final CheckError error, final List<CheckError> errors) {
    return errors.stream()
        .anyMatch(other -> other.begin() < error.end() && error.begin() < other.end());
  }
}
