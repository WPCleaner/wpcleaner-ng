package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

class PageAnalysisTagTest {

  @DisplayName("should analyze comment")
  @Test
  void analyzeComment() {
    // GIVEN
    final String text = "<!-- COMMENT -->";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).hasSize(1);
    Assertions.assertThat(analysis.getComments().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getComments().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should ignore internal links in every comment")
  @Test
  void analyzeInternalLinksInSeveralComments() {
    // GIVEN
    final String text = "<!-- [[A]] --><!-- [[B]] -->[[C]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                28, text.length(), "C", new PageTitle(0, "C"), null, null, false));
  }

  @DisplayName("should analyze tag attributes")
  @Test
  void analyzeTagAttributes() {
    // GIVEN
    final String text = "<span class=\"a b\" title = 'c' data-x=y hidden>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(
                0,
                text.length(),
                "span",
                TagKind.START,
                List.of(
                    new TagAttribute("class", "a b", 13),
                    new TagAttribute("title", "c", 27),
                    new TagAttribute("data-x", "y", 37),
                    new TagAttribute("hidden", null, 45))));
  }

  @DisplayName("should analyze tag with a slash in an attribute value")
  @Test
  void analyzeTagWithSlashInAttributeValue() {
    // GIVEN
    final String text = "<a href=\"https://example.com/\" rel=nofollow/>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(
                0,
                text.length(),
                "a",
                TagKind.SELF_CLOSING,
                List.of(
                    new TagAttribute("href", "https://example.com/", 9),
                    new TagAttribute("rel", "nofollow", 35))));
  }

  @DisplayName("should analyze tag kinds")
  @Test
  void analyzeTagKinds() {
    // GIVEN
    final String text = "<b><br/></B >";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(0, 3, "b", TagKind.START, List.of()),
            new TagElement(3, 8, "br", TagKind.SELF_CLOSING, List.of()),
            new TagElement(8, 13, "B", TagKind.END, List.of()));
  }

  @DisplayName("should not analyze tag when name does not start with a letter")
  @Test
  void analyzeTagNotStartingWithLetter() {
    // GIVEN
    final String text = "x <5 and y> 3";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).isEmpty();
  }

  @DisplayName("should match start and end tags")
  @Test
  void analyzeCompleteTags() {
    // GIVEN
    final String text = "<b><span><span></span></span><br></B>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final List<TagElement> tags = analysis.getTags();
    Assertions.assertThat(analysis.getCompleteTags())
        .containsExactly(
            new CompleteTagElement(tags.get(0), tags.get(6)),
            new CompleteTagElement(tags.get(1), tags.get(4)),
            new CompleteTagElement(tags.get(2), tags.get(3)));
  }

  @DisplayName("should not match unclosed tags")
  @Test
  void analyzeUnclosedTags() {
    // GIVEN
    final String text = "<b>Bold</i>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).hasSize(2);
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
  }

  @DisplayName("should not analyze tags nor links inside nowiki")
  @Test
  void analyzeNowiki() {
    // GIVEN
    final String text = "<NoWiki><b>[[Link]]</b></nowiki>[[Other]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final TagElement startTag = new TagElement(0, 8, "NoWiki", TagKind.START, List.of());
    final TagElement endTag = new TagElement(23, 32, "nowiki", TagKind.END, List.of());
    Assertions.assertThat(analysis.getTags()).containsExactly(startTag, endTag);
    Assertions.assertThat(analysis.getCompleteTags())
        .containsExactly(new CompleteTagElement(startTag, endTag));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                32, text.length(), "Other", new PageTitle(0, "Other"), null, null, false));
  }

  @DisplayName("should not analyze links inside pre")
  @Test
  void analyzePre() {
    // GIVEN
    final String text = "<pre>[[Link]]</pre>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze links after unclosed nowiki")
  @Test
  void analyzeUnclosedNowiki() {
    // GIVEN
    final String text = "<nowiki>[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                8, text.length(), "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should analyze links inside tags with wiki content")
  @Test
  void analyzeLinkInsideRef() {
    // GIVEN
    final String text = "<ref name=\"a\">[[Link]]</ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(14, 22, "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should not analyze links inside tag attributes")
  @Test
  void analyzeLinkInsideTagAttribute() {
    // GIVEN
    final String text = "<span title=\"[[Link]]\">";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze internal link followed by a comment")
  @Test
  void analyzeInternalLinkFollowedByComment() {
    // GIVEN
    final String text = "[[Link]]<!-- comment -->";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, 8, "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should end nowiki before a comment it contains")
  @Test
  void analyzeCommentStartInsideNowiki() {
    // GIVEN
    final String text = "<nowiki><!--</nowiki>-->[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).isEmpty();
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                24, text.length(), "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should not start nowiki inside a comment")
  @Test
  void analyzeNowikiStartInsideComment() {
    // GIVEN
    final String text = "<!--<nowiki>-->[[Link]]</nowiki>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).containsExactly(new CommentElement(0, 15));
    Assertions.assertThat(analysis.getTags())
        .containsExactly(new TagElement(23, text.length(), "nowiki", TagKind.END, List.of()));
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(15, 23, "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should not detect comments inside nowiki")
  @Test
  void analyzeCommentInsideNowiki() {
    // GIVEN
    final String text = "<nowiki><!-- comment --></nowiki>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).isEmpty();
  }

  @DisplayName("should detect comments inside tags with wiki content")
  @Test
  void analyzeCommentInsideRef() {
    // GIVEN
    final String text = "<ref><!-- [[Link]] --></ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).containsExactly(new CommentElement(5, 22));
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should end an extension tag at its first end tag")
  @Test
  void analyzeNestedExtensionTags() {
    // GIVEN
    final String text = "<ref>a<ref>b</ref>c</ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final TagElement outerStartTag = new TagElement(0, 5, "ref", TagKind.START, List.of());
    final TagElement innerStartTag = new TagElement(6, 11, "ref", TagKind.START, List.of());
    final TagElement firstEndTag = new TagElement(12, 18, "ref", TagKind.END, List.of());
    final TagElement secondEndTag = new TagElement(19, 25, "ref", TagKind.END, List.of());
    Assertions.assertThat(analysis.getTags())
        .containsExactly(outerStartTag, innerStartTag, firstEndTag, secondEndTag);
    Assertions.assertThat(analysis.getCompleteTags())
        .containsExactly(new CompleteTagElement(outerStartTag, firstEndTag));
  }

  @DisplayName("should run an unclosed comment to the end of the text")
  @Test
  void analyzeUnclosedComment() {
    // GIVEN
    final String text = "[[Link]]<!-- [[Other]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments())
        .containsExactly(new CommentElement(8, text.length()));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, 8, "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should analyze self-closing extension tag")
  @Test
  void analyzeSelfClosingExtensionTag() {
    // GIVEN
    final String text = "<ref name=\"a\" />[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(
                0, 16, "ref", TagKind.SELF_CLOSING, List.of(new TagAttribute("name", "a", 11))));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                16, text.length(), "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should not analyze content of unknown extension tags")
  @Test
  void analyzeUnknownExtensionTag() {
    // GIVEN
    final String text = "<inputbox>[[Link]]</inputbox>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze nowiki content when the wiki has no nowiki extension tag")
  @Test
  void analyzeNowikiWithoutExtensionTag() {
    // GIVEN
    final String text = "<nowiki>[[Link]]</nowiki>";

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            PageAnalysisTestFactory.WIKI,
            "Title",
            text,
            new ExtensionTagRepository(),
            new InterwikiRepository(),
            new MagicLinkRepository(),
            new MagicWordRepository(),
            new NamespaceRepository(),
            new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(8, 16, "Link", new PageTitle(0, "Link"), null, null, false));
  }
}
