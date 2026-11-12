package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.timeline.TimelineLinkElement;
import org.wpcleaner.api.analysis.timeline.TimelineLinkType;

class PageAnalysisTimelineTest {

  @DisplayName("should analyze links in timelines outside comments")
  @Test
  void analyzeTimelineLinks() {
    // GIVEN
    final String paris = "[[Paris]]";
    final String lyon = "link:https://fr.wikipedia.org/wiki/Lyon";
    final String nantes = "[[Nantes|N]]";
    final String example = "[https://example.org/a#b|ex]";
    final String text =
        "<timeline>\n"
            + "ImageSize = width:400 # [[Commented]] link:https://a.org/\n"
            + "#> [[Block]]\n[https://b.org/|comment] <#\n"
            + "PlotData=\n"
            + "  bar:A text:"
            + paris
            + " "
            + lyon
            + "#Histoire\n"
            + "  bar:B text:\""
            + nantes
            + " et "
            + example
            + "\" Link:Marseille\n"
            + "  bar:C text:[[Unclosed\n"
            + "  bar:D textlink:Toulouse]] link: \n"
            + "  bar:E text:\"Quoted # [[Visible]] link:Hidden\"\n"
            + "  bar:F text:\"Unclosed\n"
            + "  # [[Hidden]]\n"
            + "  bar:G text:[[Outside</timeline>]]\n"
            + "<pre>[[Pre]]</pre>[[Rennes]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTimelineLinks())
        .containsExactly(
            new TimelineLinkElement(
                text.indexOf(paris),
                text.indexOf(paris) + paris.length(),
                TimelineLinkType.INTERNAL_LINK,
                "Paris",
                null),
            new TimelineLinkElement(
                text.indexOf(lyon),
                text.indexOf(lyon) + lyon.length(),
                TimelineLinkType.LINK_ATTRIBUTE,
                "https://fr.wikipedia.org/wiki/Lyon",
                null),
            new TimelineLinkElement(
                text.indexOf(nantes),
                text.indexOf(nantes) + nantes.length(),
                TimelineLinkType.INTERNAL_LINK,
                "Nantes",
                "N"),
            new TimelineLinkElement(
                text.indexOf(example),
                text.indexOf(example) + example.length(),
                TimelineLinkType.EXTERNAL_LINK,
                "https://example.org/a#b",
                "ex"),
            new TimelineLinkElement(
                text.indexOf("Link:Marseille"),
                text.indexOf("Link:Marseille") + "Link:Marseille".length(),
                TimelineLinkType.LINK_ATTRIBUTE,
                "Marseille",
                null),
            new TimelineLinkElement(
                text.indexOf("[[Visible]]"),
                text.indexOf("[[Visible]]") + "[[Visible]]".length(),
                TimelineLinkType.INTERNAL_LINK,
                "Visible",
                null));
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::title)
        .containsExactly(new PageTitle(0, "Rennes"));
  }
}
