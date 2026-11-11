package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.WikiTagType;

final class GalleryCaptions {

  private static final String CAPTION = "caption";

  private GalleryCaptions() {}

  static List<Area> find(final List<CompleteTagElement> completeTags) {
    return completeTags.stream()
        .map(CompleteTagElement::startTag)
        .filter(tag -> WikiTagType.GALLERY.getNormalizedName().equals(tag.normalizedName()))
        .flatMap(tag -> tag.attributes().stream())
        .filter(attribute -> CAPTION.equals(attribute.name().toLowerCase(Locale.ROOT)))
        .filter(attribute -> Objects.nonNull(attribute.value()))
        .map(GalleryCaptions::toArea)
        .toList();
  }

  static List<Element> excludeOutside(
      final List<? extends Element> exclusions, final List<Area> captions) {
    final List<Element> result = new ArrayList<>();
    for (final Element exclusion : exclusions) {
      int begin = exclusion.begin();
      for (final Area caption : captions) {
        if (caption.begin() >= begin && caption.end() <= exclusion.end()) {
          result.add(new Area(begin, caption.begin()));
          begin = caption.end();
        }
      }
      result.add(new Area(begin, exclusion.end()));
    }
    return result;
  }

  private static Area toArea(final TagAttribute attribute) {
    return new Area(
        attribute.valueBegin(),
        attribute.valueBegin() + Objects.requireNonNull(attribute.value()).length());
  }
}
