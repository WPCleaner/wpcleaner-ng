package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagBuilder;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.template.TemplateBuilder;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

record SelfClosedTags(
    PageAnalysis analysis,
    ClearReplacements clearReplacements,
    List<AnchorTemplate> anchorTemplates) {

  private static final Set<HtmlTagType> ANCHOR_TAGS =
      Set.of(HtmlTagType.CITE, HtmlTagType.DIV, HtmlTagType.SPAN);
  private static final String ID = "id";

  SelfClosedTags {
    anchorTemplates = List.copyOf(anchorTemplates);
  }

  List<CheckError> analyze(final int algorithm) {
    return analysis.getTags().stream()
        .filter(tag -> tag.kind() == TagKind.SELF_CLOSING)
        .flatMap(
            tag ->
                tag.type().stream()
                    .filter(HtmlTagType.class::isInstance)
                    .map(HtmlTagType.class::cast)
                    .filter(type -> !type.isVoid())
                    .map(type -> createError(algorithm, tag, type)))
        .toList();
  }

  private CheckError createError(
      final int algorithm, final TagElement tag, final HtmlTagType type) {
    final List<Replacement> replacements = new ArrayList<>();
    if (isAfterUnclosedStartTag(tag)) {
      replacements.add(
          new Replacement(
              TagBuilder.from(tag.normalizedName()).withKind(TagKind.END).build(), false));
    }
    if (type == HtmlTagType.DIV) {
      ClearReplacements.findValue(tag)
          .flatMap(clearReplacements::findReplacement)
          .ifPresent(replacement -> replacements.add(new Replacement(replacement, false)));
    }
    if (ANCHOR_TAGS.contains(type)) {
      findOnlyId(tag)
          .ifPresent(
              id ->
                  anchorTemplates.forEach(
                      anchor ->
                          replacements.add(
                              new Replacement(
                                  TemplateBuilder.from(anchor.template())
                                      .withParameter(anchor.parameter(), id)
                                      .build(),
                                  false))));
    }
    replacements.add(new Replacement("", false));
    return new CheckError(algorithm, tag.begin(), tag.end(), replacements);
  }

  private boolean isAfterUnclosedStartTag(final TagElement tag) {
    return analysis.getTags().stream()
        .filter(other -> other.normalizedName().equals(tag.normalizedName()))
        .filter(other -> other.end() <= tag.begin())
        .reduce((_, last) -> last)
        .filter(previous -> previous.kind() == TagKind.START)
        .filter(
            previous ->
                analysis.getCompleteTags().stream()
                    .noneMatch(complete -> complete.startTag().equals(previous)))
        .isPresent();
  }

  private static Optional<String> findOnlyId(final TagElement tag) {
    if (tag.attributes().stream().anyMatch(attribute -> !ID.equalsIgnoreCase(attribute.name()))) {
      return Optional.empty();
    }
    return tag.attributes().stream()
        .map(TagAttribute::value)
        .filter(value -> value != null && !value.isBlank())
        .map(String::trim)
        .findFirst();
  }
}
