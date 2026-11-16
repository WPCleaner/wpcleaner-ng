package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a011;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.HtmlCharacter;
import org.wpcleaner.lib.check.algorithm.HtmlNamedEntity;

@Component
public class CheckAlgorithm011 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 11;
  private static final String MATH_TEMPLATE = "math";
  private static final List<HtmlCharacter> HTML_CHARACTERS =
      List.of(
          HtmlCharacter.LETTER_SMALL_A_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_A_ACUTE_ACCENT,
          HtmlCharacter.LETTER_SMALL_A_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_A_CIRCUMFLEX_ACCENT,
          HtmlCharacter.SYMBOL_SPACING_ACUTE,
          HtmlCharacter.LETTER_SMALL_AE,
          HtmlCharacter.LETTER_CAPITAL_AE,
          HtmlCharacter.LETTER_SMALL_A_GRAVE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_A_GRAVE_ACCENT,
          HtmlCharacter.LETTER_SMALL_ALPHA,
          HtmlCharacter.LETTER_SMALL_A_RING,
          HtmlCharacter.LETTER_CAPITAL_A_RING,
          HtmlCharacter.SYMBOL_ALMOST_EQUAL,
          HtmlCharacter.LETTER_SMALL_A_TILDE,
          HtmlCharacter.LETTER_CAPITAL_A_TILDE,
          HtmlCharacter.LETTER_SMALL_A_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_A_UMLAUT_MARK,
          HtmlCharacter.SYMBOL_DOUBLE_LOW_9_QUOTATION_MARK,
          HtmlCharacter.LETTER_SMALL_BETA,
          HtmlCharacter.SYMBOL_BROKEN_VERTICAL_BAR,
          HtmlCharacter.SYMBOL_BULLET,
          HtmlCharacter.LETTER_SMALL_C_CEDILLA,
          HtmlCharacter.LETTER_CAPITAL_C_CEDILLA,
          HtmlCharacter.SYMBOL_CENT,
          HtmlCharacter.LETTER_SMALL_CHI,
          HtmlCharacter.SYMBOL_CLUB,
          HtmlCharacter.SYMBOL_COPYRIGHT,
          HtmlCharacter.SYMBOL_CARRIAGE_RETURN_ARROW,
          HtmlCharacter.SYMBOL_DOUBLE_DAGGER,
          HtmlCharacter.SYMBOL_DOWN_ARROW,
          HtmlCharacter.SYMBOL_DOWN_DOUBLE_ARROW,
          HtmlCharacter.SYMBOL_DEGREE,
          HtmlCharacter.LETTER_SMALL_DELTA,
          HtmlCharacter.LETTER_CAPITAL_DELTA,
          HtmlCharacter.SYMBOL_DIAMOND,
          HtmlCharacter.SYMBOL_DIVISION,
          HtmlCharacter.LETTER_SMALL_E_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_E_ACUTE_ACCENT,
          HtmlCharacter.LETTER_SMALL_E_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_E_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_SMALL_E_GRAVE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_E_GRAVE_ACCENT,
          HtmlCharacter.LETTER_SMALL_EPSILON,
          HtmlCharacter.SYMBOL_EQUIVALENT,
          HtmlCharacter.LETTER_SMALL_ETA,
          HtmlCharacter.LETTER_SMALL_ETH,
          HtmlCharacter.LETTER_CAPITAL_ETH,
          HtmlCharacter.LETTER_SMALL_E_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_E_UMLAUT_MARK,
          HtmlCharacter.SYMBOL_EURO,
          HtmlCharacter.LETTER_F_WITH_HOOK,
          HtmlCharacter.SYMBOL_FRACTION_1_2,
          HtmlCharacter.SYMBOL_FRACTION_1_4,
          HtmlCharacter.SYMBOL_FRACTION_3_4,
          HtmlCharacter.SYMBOL_FRACTION_SLASH,
          HtmlCharacter.LETTER_SMALL_GAMMA,
          HtmlCharacter.LETTER_CAPITAL_GAMMA,
          HtmlCharacter.SYMBOL_GREATER_OR_EQUAL,
          HtmlCharacter.SYMBOL_LEFT_RIGHT_ARROW,
          HtmlCharacter.SYMBOL_LEFT_RIGHT_DOUBLE_ARROW,
          HtmlCharacter.SYMBOL_HEART,
          HtmlCharacter.SYMBOL_HORIZONTAL_ELLIPSIS,
          HtmlCharacter.LETTER_SMALL_I_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_I_ACUTE_ACCENT,
          HtmlCharacter.LETTER_SMALL_I_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_I_CIRCUMFLEX_ACCENT,
          HtmlCharacter.SYMBOL_INVERTED_EXCLAMATION_MARK,
          HtmlCharacter.LETTER_SMALL_I_GRAVE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_I_GRAVE_ACCENT,
          HtmlCharacter.SYMBOL_INFINITY,
          HtmlCharacter.SYMBOL_INTEGRAL,
          HtmlCharacter.LETTER_SMALL_IOTA,
          HtmlCharacter.LETTER_CAPITAL_IOTA,
          HtmlCharacter.SYMBOL_INVERTED_QUESTION_MARK,
          HtmlCharacter.LETTER_SMALL_I_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_I_UMLAUT_MARK,
          HtmlCharacter.LETTER_SMALL_KAPPA,
          HtmlCharacter.LETTER_SMALL_LAMBDA,
          HtmlCharacter.LETTER_CAPITAL_LAMBDA,
          HtmlCharacter.SYMBOL_LEFT_ANGLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_LEFT_ARROW,
          HtmlCharacter.SYMBOL_LEFT_DOUBLE_ARROW,
          HtmlCharacter.SYMBOL_LEFT_DOUBLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_LESS_OR_EQUAL,
          HtmlCharacter.SYMBOL_LOZENGE,
          HtmlCharacter.SYMBOL_SINGLE_LEFT_ANGLE_QUOTATION,
          HtmlCharacter.SYMBOL_LEFT_SINGLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_MICRO,
          HtmlCharacter.SYMBOL_MIDDLE_DOT,
          HtmlCharacter.SYMBOL_MINUS,
          HtmlCharacter.LETTER_SMALL_MU,
          HtmlCharacter.SYMBOL_NOT_EQUAL,
          HtmlCharacter.SYMBOL_NEGATION,
          HtmlCharacter.LETTER_SMALL_N_TILDE,
          HtmlCharacter.LETTER_CAPITAL_N_TILDE,
          HtmlCharacter.LETTER_SMALL_O_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_O_ACUTE_ACCENT,
          HtmlCharacter.LETTER_SMALL_O_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_O_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_SMALL_OE,
          HtmlCharacter.LETTER_CAPITAL_OE,
          HtmlCharacter.LETTER_SMALL_O_GRAVE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_O_GRAVE_ACCENT,
          HtmlCharacter.SYMBOL_OVERLINE,
          HtmlCharacter.LETTER_SMALL_OMEGA,
          HtmlCharacter.LETTER_CAPITAL_OMEGA,
          HtmlCharacter.SYMBOL_FEMININE_ORDINAL_INDICATOR,
          HtmlCharacter.SYMBOL_MASCULINE_ORDINAL_INDICATOR,
          HtmlCharacter.LETTER_SMALL_O_SLASH,
          HtmlCharacter.LETTER_CAPITAL_O_SLASH,
          HtmlCharacter.LETTER_SMALL_O_TILDE,
          HtmlCharacter.LETTER_CAPITAL_O_TILDE,
          HtmlCharacter.LETTER_SMALL_O_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_O_UMLAUT_MARK,
          HtmlCharacter.SYMBOL_PARAGRAPH,
          HtmlCharacter.SYMBOL_PART,
          HtmlCharacter.SYMBOL_PER_MILLE,
          HtmlCharacter.LETTER_SMALL_PHI,
          HtmlCharacter.LETTER_CAPITAL_PHI,
          HtmlCharacter.LETTER_SMALL_PI,
          HtmlCharacter.LETTER_CAPITAL_PI,
          HtmlCharacter.LETTER_PI_SYMBOL,
          HtmlCharacter.SYMBOL_PLUS_OR_MINUS,
          HtmlCharacter.SYMBOL_POUND,
          HtmlCharacter.SYMBOL_MINUTES,
          HtmlCharacter.SYMBOL_SECONDS,
          HtmlCharacter.SYMBOL_PROD,
          HtmlCharacter.LETTER_SMALL_PSI,
          HtmlCharacter.LETTER_CAPITAL_PSI,
          HtmlCharacter.SYMBOL_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_SQUARE_ROOT,
          HtmlCharacter.SYMBOL_RIGHT_ANGLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_RIGHT_ARROW,
          HtmlCharacter.SYMBOL_RIGHT_DOUBLE_ARROW,
          HtmlCharacter.SYMBOL_RIGHT_DOUBLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_REGISTERED_TRADEMARK,
          HtmlCharacter.LETTER_SMALL_RHO,
          HtmlCharacter.SYMBOL_SINGLE_RIGHT_ANGLE_QUOTATION,
          HtmlCharacter.SYMBOL_RIGHT_SINGLE_QUOTATION_MARK,
          HtmlCharacter.SYMBOL_SINGLE_LOW_9_QUOTATION_MARK,
          HtmlCharacter.LETTER_SMALL_S_CARON,
          HtmlCharacter.LETTER_CAPITAL_S_CARON,
          HtmlCharacter.SYMBOL_SECTION,
          HtmlCharacter.LETTER_SMALL_SIGMA,
          HtmlCharacter.LETTER_CAPITAL_SIGMA,
          HtmlCharacter.LETTER_SMALL_SIGMAF,
          HtmlCharacter.SYMBOL_SPADE,
          HtmlCharacter.SYMBOL_SUM,
          HtmlCharacter.SYMBOL_SUPERSCRIPT_1,
          HtmlCharacter.SYMBOL_SUPERSCRIPT_2,
          HtmlCharacter.SYMBOL_SUPERSCRIPT_3,
          HtmlCharacter.LETTER_SMALL_SHARP_S,
          HtmlCharacter.LETTER_SMALL_TAU,
          HtmlCharacter.LETTER_SMALL_THETA,
          HtmlCharacter.LETTER_CAPITAL_THETA,
          HtmlCharacter.LETTER_THETA_SYMBOL,
          HtmlCharacter.LETTER_SMALL_THORN,
          HtmlCharacter.LETTER_CAPITAL_THORN,
          HtmlCharacter.SYMBOL_SMALL_TILDE,
          HtmlCharacter.SYMBOL_MULTIPLICATION,
          HtmlCharacter.SYMBOL_TRADEMARK,
          HtmlCharacter.LETTER_SMALL_U_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_U_ACUTE_ACCENT,
          HtmlCharacter.SYMBOL_UP_ARROW,
          HtmlCharacter.SYMBOL_UP_DOUBLE_ARROW,
          HtmlCharacter.LETTER_SMALL_U_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_U_CIRCUMFLEX_ACCENT,
          HtmlCharacter.LETTER_SMALL_U_GRAVE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_U_GRAVE_ACCENT,
          HtmlCharacter.LETTER_UPSILON_SYMBOL,
          HtmlCharacter.LETTER_SMALL_U_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_U_UMLAUT_MARK,
          HtmlCharacter.LETTER_SMALL_XI,
          HtmlCharacter.LETTER_CAPITAL_XI,
          HtmlCharacter.LETTER_SMALL_Y_ACUTE_ACCENT,
          HtmlCharacter.LETTER_CAPITAL_Y_ACUTE_ACCENT,
          HtmlCharacter.SYMBOL_YEN,
          HtmlCharacter.LETTER_SMALL_Y_UMLAUT_MARK,
          HtmlCharacter.LETTER_CAPITAL_Y_UMLAUT_MARK,
          HtmlCharacter.LETTER_SMALL_ZETA);

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (hasMathTag(analysis) || hasMathTemplate(analysis)) {
      return List.of();
    }
    return HtmlNamedEntity.findWithSemicolon(analysis, HTML_CHARACTERS).stream()
        .map(entity -> createError(analysis, entity))
        .toList();
  }

  private static boolean hasMathTag(final PageAnalysis analysis) {
    return analysis.getTags().stream()
        .anyMatch(tag -> tag.type().filter(type -> type == WikiTagType.MATH).isPresent());
  }

  private static boolean hasMathTemplate(final PageAnalysis analysis) {
    final PageTitle math =
        analysis.normalizeTitle(MATH_TEMPLATE, CommonNamespaces.TEMPLATE.id).orElse(null);
    return analysis.getTemplates().stream().anyMatch(template -> template.title().equals(math));
  }

  private static CheckError createError(final PageAnalysis analysis, final HtmlNamedEntity entity) {
    return new CheckError(
        ALGORITHM_NUMBER, entity.begin(), entity.end(), createReplacements(analysis, entity));
  }

  private static List<Replacement> createReplacements(
      final PageAnalysis analysis, final HtmlNamedEntity entity) {
    final String value = String.valueOf(entity.character().getValue());
    if (entity.character() != HtmlCharacter.SYMBOL_QUOTATION_MARK) {
      return List.of(new Replacement(value, true));
    }
    if (isInOne(entity, analysis.getTags())) {
      return List.of();
    }
    return List.of(new Replacement(value, !isInOne(entity, analysis.getTables())));
  }

  private static boolean isInOne(
      final HtmlNamedEntity entity, final List<? extends Element> elements) {
    return elements.stream()
        .anyMatch(element -> element.begin() <= entity.begin() && entity.begin() < element.end());
  }
}
