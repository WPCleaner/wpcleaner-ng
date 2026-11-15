package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a004;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.ArticleUrl;

@Component
public class CheckAlgorithm004 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 4;
  private static final String TAG_NAME = HtmlTagType.A.getNormalizedName();
  private static final String HREF = "href";
  private static final String TELEPHONE_PROTOCOL = "tel:";
  private static final Set<String> IGNORED_TAG_NAMES =
      Set.of(HtmlTagType.CODE.getNormalizedName(), WikiTagType.IMAGEMAP.getNormalizedName());

  private final ProtocolRepository protocolRepository;

  public CheckAlgorithm004(final ProtocolRepository protocolRepository) {
    this.protocolRepository = protocolRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() != CommonNamespaces.MAIN.id) {
      return List.of();
    }
    final List<CompleteTagElement> ignoredAreas =
        analysis.getCompleteTags().stream()
            .filter(tag -> IGNORED_TAG_NAMES.contains(tag.startTag().normalizedName()))
            .toList();
    final List<CompleteTagElement> completeTags =
        analysis.getCompleteTags().stream()
            .filter(tag -> TAG_NAME.equals(tag.startTag().normalizedName()))
            .toList();
    final List<Protocol> protocols = protocolRepository.getProtocols(analysis.getWiki());
    return analysis.getTags().stream()
        .filter(tag -> TAG_NAME.equals(tag.normalizedName()))
        .filter(tag -> !tag.isInsideOne(ignoredAreas))
        .filter(tag -> !isCompleteEndTag(tag, completeTags))
        .map(tag -> createError(analysis, tag, completeTags, protocols))
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private static boolean isCompleteEndTag(
      final TagElement tag, final List<CompleteTagElement> completeTags) {
    return tag.kind() == TagKind.END
        && completeTags.stream().anyMatch(complete -> complete.endTag().equals(tag));
  }

  private static CheckError createError(
      final PageAnalysis analysis,
      final TagElement tag,
      final List<CompleteTagElement> completeTags,
      final List<Protocol> protocols) {
    final Optional<CompleteTagElement> complete =
        completeTags.stream().filter(candidate -> candidate.startTag().equals(tag)).findFirst();
    if (complete.isEmpty()) {
      return new CheckError(
          ALGORITHM_NUMBER, tag.begin(), tag.end(), List.of(new Replacement("", false)));
    }
    final String internalText =
        analysis.getText().substring(tag.end(), complete.get().endTag().begin());
    return new CheckError(
        ALGORITHM_NUMBER,
        complete.get().begin(),
        complete.get().end(),
        createReplacements(analysis, tag, internalText, protocols));
  }

  private static List<Replacement> createReplacements(
      final PageAnalysis analysis,
      final TagElement tag,
      final String internalText,
      final List<Protocol> protocols) {
    final List<Replacement> replacements = new ArrayList<>();
    final Optional<String> href = findHref(tag);
    href.filter(value -> value.startsWith(TELEPHONE_PROTOCOL))
        .ifPresent(_ -> replacements.add(new Replacement(internalText, true)));
    href.flatMap(value -> ArticleUrl.of(analysis.getWiki(), value))
        .map(url -> createArticleReplacement(url, internalText))
        .ifPresent(replacements::add);
    href.filter(value -> isProtocol(protocols, value))
        .ifPresent(
            value ->
                replacements.add(new Replacement("[" + value + " " + internalText + "]", false)));
    final Replacement plainText = new Replacement(internalText, false);
    if (replacements.stream().map(Replacement::text).noneMatch(internalText::equals)) {
      replacements.add(plainText);
    }
    return replacements;
  }

  private static Replacement createArticleReplacement(
      final ArticleUrl url, final String internalText) {
    return new Replacement(
        InternalLinkBuilder.to(url.title())
            .withAnchor(url.fragment())
            .withText(internalText)
            .build(),
        url.attributes().isEmpty() && url.fragment() == null);
  }

  private static Optional<String> findHref(final TagElement tag) {
    return tag.attributes().stream()
        .filter(attribute -> HREF.equalsIgnoreCase(attribute.name()))
        .findFirst()
        .map(TagAttribute::value)
        .map(String::trim)
        .filter(value -> !value.isEmpty());
  }

  private static boolean isProtocol(final List<Protocol> protocols, final String value) {
    return protocols.stream()
        .anyMatch(
            protocol ->
                value.regionMatches(true, 0, protocol.value(), 0, protocol.value().length()));
  }
}
