package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a030;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.ExternalView;
import org.wpcleaner.lib.check.TextInput;

@Component
public class CheckAlgorithm030 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 30;
  private static final String ALTERNATE_DESCRIPTION = "img_alt";
  private static final String IMAGE_END = "]]";
  private static final String PARAMETER_SEPARATOR = "|";
  private static final String ALTERNATE_DESCRIPTION_PREFIX = "alt=";
  private static final String UNAUTHORIZED_CHARACTERS = "[]|=";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() != CommonNamespaces.MAIN.id) {
      return List.of();
    }
    return analysis.getImages().stream()
        .filter(image -> isBlank(image.findCaption().map(ImageParameter::value)))
        .filter(
            image ->
                isBlank(image.findParameter(ALTERNATE_DESCRIPTION).map(ImageParameter::argument)))
        .map(image -> createError(analysis, image))
        .toList();
  }

  private static boolean isBlank(final Optional<String> description) {
    return description.filter(text -> !text.isBlank()).isEmpty();
  }

  private static CheckError createError(final PageAnalysis analysis, final ImageElement image) {
    final String text = analysis.getText();
    return new CheckError(
        ALGORITHM_NUMBER,
        image.begin(),
        image.end(),
        List.of(
            new TextInput(
                GT._T("Add a description..."),
                GT._T("What description would you like to use for the image?"),
                createPrefix(text, image, _ -> true) + PARAMETER_SEPARATOR,
                IMAGE_END,
                UNAUTHORIZED_CHARACTERS),
            new TextInput(
                GT._T("Add an alternate description..."),
                GT._T("What alternate description would you like to use for the image?"),
                createPrefix(
                        text,
                        image,
                        parameter -> !ALTERNATE_DESCRIPTION.equals(parameter.magicWord()))
                    + PARAMETER_SEPARATOR
                    + ALTERNATE_DESCRIPTION_PREFIX,
                IMAGE_END,
                UNAUTHORIZED_CHARACTERS),
            new ExternalView(
                GT._T("View image"),
                analysis.getWiki().pageUrl(image.namespace() + ":" + image.name()))));
  }

  private static String createPrefix(
      final String text, final ImageElement image, final Predicate<ImageParameter> keptParameter) {
    final int nameEnd =
        image.parameters().stream()
            .findFirst()
            .map(parameter -> parameter.begin() - PARAMETER_SEPARATOR.length())
            .orElse(image.end() - IMAGE_END.length());
    return text.substring(image.begin(), nameEnd)
        + image.parameters().stream()
            .filter(parameter -> !parameter.value().isBlank())
            .filter(keptParameter)
            .map(parameter -> PARAMETER_SEPARATOR + parameter.value())
            .collect(Collectors.joining());
  }
}
