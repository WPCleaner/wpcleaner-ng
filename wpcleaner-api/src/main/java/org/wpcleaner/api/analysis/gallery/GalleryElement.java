package org.wpcleaner.api.analysis.gallery;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;

public record GalleryElement(CompleteTagElement tag, List<GalleryImage> images) implements Element {

  public GalleryElement {
    images = List.copyOf(images);
  }

  @Override
  public int begin() {
    return tag.begin();
  }

  @Override
  public int end() {
    return tag.end();
  }
}
