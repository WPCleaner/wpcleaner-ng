package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.hook.login.ConfigurationPageReader;
import org.wpcleaner.api.hook.login.LoginWarning;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckAlgorithmParametersReader {

  private static final String DEFAULT_PROJECT_NAMESPACE = "Project";
  private static final String YAML_TAG = "syntaxhighlight";
  private static final String YAML_LANGUAGE_ATTRIBUTE = "lang";
  private static final String YAML_LANGUAGE = "yaml";
  private static final Pattern PARAMETERS_PAGE_NUMBER = Pattern.compile("/Configuration/(\\d{3})$");

  private final PageAnalysisFactory analysisFactory;
  private final NamespaceRepository namespaceRepository;
  private final ConfigurationPageReader pageReader;

  public CheckAlgorithmParametersReader(
      final PageAnalysisFactory analysisFactory,
      final NamespaceRepository namespaceRepository,
      final ConfigurationPageReader pageReader) {
    this.analysisFactory = analysisFactory;
    this.namespaceRepository = namespaceRepository;
    this.pageReader = pageReader;
  }

  public static String getParametersPage(final List<Namespace> namespaces, final int number) {
    return "%s:WPCleanerNG/Configuration/%03d"
        .formatted(
            Namespace.findNamespace(namespaces, CommonNamespaces.PROJECT.id)
                .map(Namespace::name)
                .orElse(DEFAULT_PROJECT_NAMESPACE),
            number);
  }

  public Map<Integer, CheckAlgorithmParameters> retrieveParameters(
      final WikiDefinition wiki,
      final List<Integer> numbers,
      final Consumer<LoginWarning> warnings) {
    final List<Namespace> namespaces = namespaceRepository.getNamespaces(wiki);
    final List<String> parametersPages =
        numbers.stream().map(number -> getParametersPage(namespaces, number)).toList();
    final Map<Integer, CheckAlgorithmParameters> parametersByNumber = new HashMap<>();
    pageReader
        .retrieveContents(wiki, parametersPages)
        .forEach(
            (title, content) -> {
              final Matcher matcher = PARAMETERS_PAGE_NUMBER.matcher(title);
              if (matcher.find()) {
                parseParameters(wiki, title, content, warnings)
                    .ifPresent(
                        parameters ->
                            parametersByNumber.put(Integer.parseInt(matcher.group(1)), parameters));
              }
            });
    return Map.copyOf(parametersByNumber);
  }

  private Optional<CheckAlgorithmParameters> parseParameters(
      final WikiDefinition wiki,
      final String title,
      final String content,
      final Consumer<LoginWarning> warnings) {
    final Optional<String> yaml =
        analysisFactory.analysis(wiki, title, content).getCompleteTags().stream()
            .filter(tag -> YAML_TAG.equals(tag.startTag().normalizedName()))
            .filter(CheckAlgorithmParametersReader::isYaml)
            .findFirst()
            .map(tag -> content.substring(tag.startTag().end(), tag.endTag().begin()));
    if (yaml.isEmpty()) {
      warnings.accept(new LoginWarning(title, GT._T("No YAML configuration found")));
      return Optional.empty();
    }
    try {
      return Optional.of(CheckAlgorithmParameters.parse(yaml.get()));
    } catch (final IllegalArgumentException e) {
      warnings.accept(new LoginWarning(title, String.valueOf(e.getMessage())));
      return Optional.empty();
    }
  }

  private static boolean isYaml(final CompleteTagElement tag) {
    return tag.startTag().attributes().stream()
        .anyMatch(
            attribute ->
                YAML_LANGUAGE_ATTRIBUTE.equalsIgnoreCase(attribute.name())
                    && YAML_LANGUAGE.equalsIgnoreCase(attribute.value()));
  }
}
