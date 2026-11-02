package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.gallery.GalleryImage;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class ImageSyntaxRule implements PageSyntaxRule {

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.IMAGE;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    return Stream.concat(
            pageAnalysis.getImages().stream()
                .map(image -> new RuleRange(image.begin(), image.end())),
            pageAnalysis.getGalleries().stream()
                .flatMap(gallery -> gallery.images().stream())
                .map(ImageSyntaxRule::computeNameRange))
        .toList();
  }

  private static RuleRange computeNameRange(final GalleryImage image) {
    return new RuleRange(
        image.begin(),
        image.parameters().isEmpty() ? image.end() : image.parameters().getFirst().begin() - 1);
  }
}
