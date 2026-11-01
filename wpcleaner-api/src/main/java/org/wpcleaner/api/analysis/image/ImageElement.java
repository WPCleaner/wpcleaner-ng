package org.wpcleaner.api.analysis.image;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;

public record ImageElement(
    int begin,
    int end,
    String namespace,
    String name,
    PageTitle title,
    List<ImageParameter> parameters)
    implements Element {

  public ImageElement {
    parameters = List.copyOf(parameters);
  }

  public Optional<ImageParameter> findCaption() {
    return parameters.reversed().stream()
        .filter(parameter -> parameter.magicWord() == null)
        .findFirst();
  }

  public Optional<ImageParameter> findParameter(final String magicWord) {
    return parameters.reversed().stream()
        .filter(parameter -> magicWord.equals(parameter.magicWord()))
        .findFirst();
  }
}
