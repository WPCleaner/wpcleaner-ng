package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a030;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.ExternalView;

@Component
public class CheckAlgorithm030 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 30;
  private static final String ALTERNATE_DESCRIPTION = "img_alt";

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
    return new CheckError(
        ALGORITHM_NUMBER,
        image.begin(),
        image.end(),
        List.of(
            new ExternalView(
                GT._T("View image"),
                analysis.getWiki().pageUrl(image.namespace() + ":" + image.name()))));
  }
}
