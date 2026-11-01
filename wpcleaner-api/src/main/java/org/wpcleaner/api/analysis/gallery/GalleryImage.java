package org.wpcleaner.api.analysis.gallery;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.image.ImageParameter;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;

public record GalleryImage(
    int begin, int end, String name, @Nullable PageTitle title, List<ImageParameter> parameters) {

  public GalleryImage {
    parameters = List.copyOf(parameters);
  }

  public Optional<ImageParameter> findCaption() {
    return ImageParameter.findCaption(parameters);
  }

  public Optional<ImageParameter> findParameter(final String magicWord) {
    return ImageParameter.findParameter(parameters, magicWord);
  }
}
