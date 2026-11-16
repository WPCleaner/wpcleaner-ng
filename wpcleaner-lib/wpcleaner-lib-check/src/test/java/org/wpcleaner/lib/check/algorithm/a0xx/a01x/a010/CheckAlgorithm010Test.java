package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm010Test {

  private static final int ALGORITHM = 10;
  private static final String ARTICLE = "Paris";
  private static final String USER_PAGE = "Utilisateur:Paris";

  @Autowired private CheckAlgorithm010 algorithm;

  @DisplayName("should not report square brackets correctly closed")
  @Test
  void ignoreClosedBrackets() {
    // GIVEN
    final String text =
        "[[A]] [[A|b]] [[A|[b]]] [[A|b [c] d]] [[Fichier:A.png|thumb|alt=a [b]|c [d] e]]"
            + " [[Catégorie:A]] [[en:A]] [[wikt:A]] [http://a.org b] [[{{a}}]] [b]";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should automatically remove brackets surrounding a link in an article")
  @Test
  void removeSurroundingBrackets() {
    // GIVEN
    final String link = "[[[[B]]]]";
    final String category = "[[[[Catégorie:C]]";
    final String manyBrackets = "[[[[[[D|e]]]]]]]";
    final String text = String.join(" ", "a", link, category, manyBrackets);

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, link, "[[B]]", true),
            error(text, category, "[[Catégorie:C]]", true),
            error(text, manyBrackets, "[[D|e]]", true));
  }

  @DisplayName("should not remove automatically surrounding brackets outside articles")
  @Test
  void removeSurroundingBracketsManuallyOutsideArticles() {
    // GIVEN
    final String text = "[[[[B]]]]";

    // WHEN
    final List<CheckError> errors = analyze(USER_PAGE, text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, text, "[[B]]", false));
  }

  @DisplayName("should suggest two brackets when extra brackets don't surround a link")
  @Test
  void reduceExtraBrackets() {
    // GIVEN
    final String text = "[[[A]]] [[[[[B";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 3, List.of(new Replacement("[[", false))),
            new CheckError(ALGORITHM, 8, 13, List.of(new Replacement("[[", false))));
  }

  @DisplayName("should suggest closing a link ended by a single bracket")
  @Test
  void closeLinkEndedBySingleBracket() {
    // GIVEN
    final String link = "[[B|c]";
    final String text = "a " + link + " d";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, link, "[[B|c]]", false));
  }

  @DisplayName("should automatically close a category alone on its line in an article")
  @Test
  void closeCategoryAutomatically() {
    // GIVEN
    final String first = "[[Catégorie:A]";
    final String second = "[[catégories : B|c]";
    final String text = first + "\nd\n" + second;

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, first, "[[Catégorie:A]]", true),
            error(text, second, "[[catégories : B|c]]", true));
  }

  @DisplayName("should not close automatically a category outside articles")
  @Test
  void closeCategoryManuallyOutsideArticles() {
    // GIVEN
    final String text = "[[Catégorie:A]";

    // WHEN
    final List<CheckError> errors = analyze(USER_PAGE, text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, text, "[[Catégorie:A]]", false));
  }

  @DisplayName("should not close automatically a link which is not a category alone on its line")
  @Test
  void closeOtherLinksManually() {
    // GIVEN
    final String afterText = "[[Catégorie:A]";
    final String beforeText = "[[Catégorie:B]";
    final String notCategory = "[[Aide:C]";
    final String noNamespace = "[[D]";
    final String leadingColon = "[[:Catégorie:E]";
    final String text =
        String.join(
            "\n", "x " + afterText, beforeText + " x", notCategory, noNamespace, leadingColon);

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, afterText, "[[Catégorie:A]]", false),
            error(text, beforeText, "[[Catégorie:B]]", false),
            error(text, notCategory, "[[Aide:C]]", false),
            error(text, noNamespace, "[[D]]", false),
            error(text, leadingColon, "[[:Catégorie:E]]", false));
  }

  @DisplayName("should suggest both positions for the end when single brackets are separated")
  @Test
  void closeLinkWithSeparatedBrackets() {
    // GIVEN
    final String link = "[[Catégorie:A]b]";
    final String text = link + "\n[[C] d [e]";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                link.length(),
                List.of(
                    new Replacement("[[Catégorie:A]]b", false),
                    new Replacement("[[Catégorie:Ab]]", false))),
            error(text, "[[C]", "[[C]]", false));
  }

  @DisplayName("should suggest a link or a template when a link is ended by curly brackets")
  @Test
  void closeLinkEndedByCurlyBrackets() {
    // GIVEN
    final String text = "[[A}} [[B} [[C}";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                5,
                List.of(new Replacement("[[A]]", false), new Replacement("{{A}}", false))),
            new CheckError(
                ALGORITHM,
                6,
                10,
                List.of(new Replacement("[[B]]", false), new Replacement("{{B}}", false))),
            new CheckError(
                ALGORITHM,
                11,
                15,
                List.of(new Replacement("[[C]]", false), new Replacement("{{C}}", false))));
  }

  @DisplayName("should report without replacement a link with no end on its line")
  @Test
  void reportLinkWithoutEnd() {
    // GIVEN
    final String text = "[[A\nb]] [[C|d [[E]] f]] [[G{{h}}] [[http://i.org";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 2, List.of()),
            new CheckError(ALGORITHM, 8, 10, List.of()),
            new CheckError(ALGORITHM, 24, 26, List.of()),
            new CheckError(ALGORITHM, 34, 36, List.of()));
  }

  @DisplayName("should not report brackets in comments, tags, code and content which is not wiki")
  @Test
  void ignoreAreasWithoutWikiText() {
    // GIVEN
    final String text =
        "<!-- [[a --> <nowiki>[[a</nowiki> <code>[[a</code> <math>[[a</math> <pre>[[a</pre>"
            + " <source>[[a</source> <syntaxhighlight>[[a</syntaxhighlight>"
            + " <ref name=\"[[a\">b</ref>";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report an unclosed link in a gallery")
  @Test
  void reportUnclosedLinkInGallery() {
    // GIVEN
    final String text = "<gallery>\nA.png|[[B]] [[C]\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, "[[C]", "[[C]]", false));
  }

  @DisplayName("should suggest removing unpaired single brackets from the text of a link")
  @Test
  void removeUnpairedBracketsFromLinkText() {
    // GIVEN
    final String opening = "[[A|b [c]]";
    final String closing = "[[A|b] c]]";
    final String nested = "[[A|b [c [d] e]]";
    final String anchor = "[[:Catégorie:A#b|c [d]]";
    final String sameText = "[[A|[A]]";
    final String text = String.join(" ", opening, closing, nested, anchor, sameText);

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, opening, "[[A|b c]]", false),
            error(text, closing, "[[A|b c]]", false),
            error(text, nested, "[[A|b c [d] e]]", false),
            error(text, anchor, "[[:Catégorie:A#b|c d]]", false),
            error(text, sameText, "[[A]]", false));
  }

  @DisplayName("should not count brackets of link texts in templates, tags and content not wiki")
  @Test
  void ignoreProtectedBracketsInLinkText() {
    // GIVEN
    final String text =
        "[[A|<nowiki>[</nowiki>b]] [[A|b<!-- ] -->]] [[A|{{b|[}}]] [[A|{{lc:[}}]]"
            + " [[A|{{{1|[}}}]] [[A|<span title=\"[\">b</span>]]";

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should suggest removing unpaired single brackets from image caption and alt")
  @Test
  void removeUnpairedBracketsFromImage() {
    // GIVEN
    final String both =
        "[[Fichier:A.png|thumb|alt=x [y|a [[B]] c] {{d|]}} <math>[</math> [[E|[f]]] [g]]]";
    final String caption = "[[Fichier:A.png|thumb|alt=x|a [b]]";
    final String alternateText = "[[Fichier:A.png|alt=x] y|thumb]]";
    final String captionFirst = "[[Fichier:A.png|a [b|alt=x] y]]";
    final String text = String.join(" ", both, caption, alternateText, captionFirst);

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                both,
                "[[Fichier:A.png|thumb|alt=x y|a [[B]] c {{d|]}} <math>[</math> [[E|[f]]] [g]]]",
                false),
            error(text, caption, "[[Fichier:A.png|thumb|alt=x|a b]]", false),
            error(text, alternateText, "[[Fichier:A.png|alt=x y|thumb]]", false),
            error(text, captionFirst, "[[Fichier:A.png|a b|alt=x y]]", false));
  }

  @DisplayName("should suggest an external link when it starts with extra brackets")
  @Test
  void removeExtraBracketsBeforeExternalLink() {
    // GIVEN
    final String two = "[[http://a.org b]";
    final String three = "[[[http://c.org]";
    final String text = String.join(" ", two, three, "[[http://d.org e]]", "[http://f.org]");

    // WHEN
    final List<CheckError> errors = analyze(ARTICLE, text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, two, "[http://a.org b]", false),
            error(text, three, "[http://c.org]", false));
  }

  @DisplayName("should report errors in the order of the text, in any namespace")
  @Test
  void reportInTextOrder() {
    // GIVEN
    final String linkText = "[[A|b [c]]";
    final String unclosed = "[[D]";
    final String externalLink = "[[http://e.org f]";
    final String image = "[[Fichier:G.png|h] i]]";
    final String text = String.join(" ", linkText, unclosed, externalLink, image, unclosed);

    // WHEN
    final List<CheckError> errors = analyze(USER_PAGE, text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(
            0,
            text.indexOf(unclosed),
            text.indexOf(externalLink),
            text.indexOf(image),
            text.lastIndexOf(unclosed));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final String text, final String element, final String replacement, final boolean automatic) {
    final int begin = text.indexOf(element);
    return new CheckError(
        ALGORITHM,
        begin,
        begin + element.length(),
        List.of(new Replacement(replacement, automatic)));
  }
}
