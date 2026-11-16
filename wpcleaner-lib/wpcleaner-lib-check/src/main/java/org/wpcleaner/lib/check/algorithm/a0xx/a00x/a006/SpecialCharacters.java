package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a006;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

final class SpecialCharacters {

  private static final String AUTHORIZED_CHARACTERS =
      "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + " 0123456789-:,.!?'#/()*";

  private static final Map<WikiDefinition, String> LOCAL_AUTHORIZED_CHARACTERS =
      Map.ofEntries(
          Map.entry(WikipediaDefinitions.CS, "čďěňřšťžČĎŇŘŠŤŽ"),
          Map.entry(WikipediaDefinitions.DA, "ÆØÅæøå"),
          Map.entry(WikipediaDefinitions.EL, "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρσςτυφχψω"),
          Map.entry(WikipediaDefinitions.FI, "ÅÄÖåäö"),
          Map.entry(WikipediaDefinitions.NO, "ÆØÅæøå"),
          Map.entry(WikipediaDefinitions.RO, "ăîâşţ"),
          Map.entry(
              WikipediaDefinitions.RU,
              "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЬЫЪЭЮЯабвгдежзийклмнопрстуфхцчшщьыъэюя"),
          Map.entry(WikipediaDefinitions.SV, "ÅÄÖåäö"),
          Map.entry(
              WikipediaDefinitions.UK,
              "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЬЫЪЭЮЯабвгдежзийклмнопрстуфхцчшщьыъэюяіїґ"));

  private static final Map<Character, String> REPLACEMENTS =
      createReplacements(
          List.of(
              new CharacterGroup("\uFEFF\u200E\u200B", ""),
              new CharacterGroup("ʻ’“”„‟′", "'"),
              new CharacterGroup("ÀÁÂÃÄÅĀĂĄǍǞǠǺȀȂȦȺАḀẠẢẤẦẨẪẬẮẰẲẴẶ", "A"),
              new CharacterGroup("àáâãäåāăąǎǟǡǻȁȃȧаḁẚạảấầẩẫậắằẳẵặⱥ", "a"),
              new CharacterGroup("ÆǢǼ", "Ae"),
              new CharacterGroup("æǣǽ", "ae"),
              new CharacterGroup("ƁɃḂḄḆ", "B"),
              new CharacterGroup("ƀɓбᶀḃḅḇ", "b"),
              new CharacterGroup("ÇĆĈĊČƇȻḈ", "C"),
              new CharacterGroup("çćĉċčƈȼḉ", "c"),
              new CharacterGroup("ÐĎĐƉƊƋḊḌḎḐ", "D"),
              new CharacterGroup("ðďđƌȡɖɗᶁḋḍḏḑ", "d"),
              new CharacterGroup("ǅǲ", "Dz"),
              new CharacterGroup("ÈÉÊËĒĔĖĘĚƎƏƐȄȆȨɆΕЁЕḘḚẸẺẼẾỀỂỄỆ", "E"),
              new CharacterGroup("èéêëēĕėęěǝȅȇȩɇəеёḕḗḙḛḝẹẻẽếềểễệ", "e"),
              new CharacterGroup("ƑḞ", "F"),
              new CharacterGroup("ƒᶂḟ", "f"),
              new CharacterGroup("ĜĞĠĢƓǤǦǴḠ", "G"),
              new CharacterGroup("ĝğġģǥǧǵɠᶃḡ", "g"),
              new CharacterGroup("ĤĦȞḢḤḦḨḪⱧΗ", "H"),
              new CharacterGroup("ĥħȟɦḣḥḧḩḫẖⱨ", "h"),
              new CharacterGroup("ÌÍÎÏĨĪĬĮİƗǏȈȊІỊ", "I"),
              new CharacterGroup("ìíîïĩīĭįıǐȉȋɨіỉị", "i"),
              new CharacterGroup("ĴɈ", "J"),
              new CharacterGroup("ĵǰɉ", "j"),
              new CharacterGroup("ĶƘǨКḰḲḴⱩ", "K"),
              new CharacterGroup("ķƙǩкᶄḱḳḵⱪ", "k"),
              new CharacterGroup("ĹĻĽĿŁȽḶḸḺⱠⱢ", "L"),
              new CharacterGroup("ĺļľŀłƚȴлᶅḷḹḻⱡ", "l"),
              new CharacterGroup("ǈ", "Lj"),
              new CharacterGroup("ǉ", "lj"),
              new CharacterGroup("МḾṀṂ", "M"),
              new CharacterGroup("ɱмᶆḿṁṃ", "m"),
              new CharacterGroup("ÑŃŅŇƝǸȠṄṆṈ", "N"),
              new CharacterGroup("ñńņňŊŋƞǹȵɲᶇṅṇṉ", "n"),
              new CharacterGroup("ǋ", "Nj"),
              new CharacterGroup("ǌ", "nj"),
              new CharacterGroup("ÒÓÔÕÖØŌŎŐƟƠǑǪǬǾȌȎȪȬȮȰОỌỎỐỒỔỖỘỚỜỞỠỢ", "O"),
              new CharacterGroup("òóôõöøōŏőơǒǫǭǿȍȏȫȭȯȱɵоọỏốồổỗộớờởỡợ", "o"),
              new CharacterGroup("Œ", "Oe"),
              new CharacterGroup("œ", "oe"),
              new CharacterGroup("ƤṔṖⱣ", "P"),
              new CharacterGroup("ƥᵽᶈṕṗ", "p"),
              new CharacterGroup("ʠ", "q"),
              new CharacterGroup("ŔŖŘȐȒɌṘṚṜṞⱤ", "R"),
              new CharacterGroup("ŕŗřȑȓɍᶉṙṛṝṟ", "r"),
              new CharacterGroup("ŚŜŞŠȘṠṢ", "S"),
              new CharacterGroup("śŝşšșȿᶊṡṣ", "s"),
              new CharacterGroup("ß", "ss"),
              new CharacterGroup("ŢŤŦƬƮȚȾṪṬṮṰ", "T"),
              new CharacterGroup("ţťŧƫƭțȶʈṫṭṯṱẗⱦ", "t"),
              new CharacterGroup("Þ", "Th"),
              new CharacterGroup("þ", "th"),
              new CharacterGroup("ÙÚÛÜŨŪŬŮŰŲƯǓǕǗǙǛȔȖɄṲṶỤỨỪỬỮỰ", "U"),
              new CharacterGroup("ùúûüũūŭůűųưǔǖǘǚǜȕȗʉṳṷụủứừửữự", "u"),
              new CharacterGroup("ƲṼṾ", "V"),
              new CharacterGroup("ʋṽṿⱴ", "v"),
              new CharacterGroup("ŴƜẀẂẄẆẈⱲ", "W"),
              new CharacterGroup("ŵẁẃẅẇẉẘⱳ", "w"),
              new CharacterGroup("ХẊẌ", "X"),
              new CharacterGroup("хᶍẋẍ", "x"),
              new CharacterGroup("ÝŶŸƳȲɎӲẎỲỴỶỸ", "Y"),
              new CharacterGroup("ýÿŷƴȳɏӳᶌẏẙỳỵỷỹ", "y"),
              new CharacterGroup("ŹŻŽƵȤẐẒẔⱫ", "Z"),
              new CharacterGroup("źżžƶȥɀʐᶎẑẓⱬ", "z"),
              new CharacterGroup("²", "2"),
              new CharacterGroup("–—−", "-"),
              new CharacterGroup("…", "...")));

  private static final Map<WikiDefinition, Map<Character, String>> LOCAL_REPLACEMENTS =
      Map.ofEntries(
          Map.entry(
              WikipediaDefinitions.EL,
              createReplacements(
                  List.of(
                      new CharacterGroup("ά", "α"),
                      new CharacterGroup("έ", "ε"),
                      new CharacterGroup("ή", "η"),
                      new CharacterGroup("ί", "ι"),
                      new CharacterGroup("ϊ", "ι"),
                      new CharacterGroup("ό", "ο"),
                      new CharacterGroup("ύ", "υ"),
                      new CharacterGroup("ϋ", "υ"),
                      new CharacterGroup("ώ", "ω"),
                      new CharacterGroup("Ά", "Α"),
                      new CharacterGroup("Έ", "Ε"),
                      new CharacterGroup("Ή", "Η"),
                      new CharacterGroup("Ί", "Ι"),
                      new CharacterGroup("Ό", "Ο"),
                      new CharacterGroup("Ύ", "Υ"),
                      new CharacterGroup("Ώ", "Ω")))),
          Map.entry(
              WikipediaDefinitions.ES,
              createReplacements(
                  List.of(new CharacterGroup("Ñ", "Nzz"), new CharacterGroup("ñ", "nzz")))));

  private SpecialCharacters() {}

  static boolean isAuthorized(final char character, final WikiDefinition wiki) {
    return AUTHORIZED_CHARACTERS.indexOf(character) >= 0
        || LOCAL_AUTHORIZED_CHARACTERS.getOrDefault(wiki, "").indexOf(character) >= 0;
  }

  static Optional<String> findReplacement(final char character, final WikiDefinition wiki) {
    return Optional.ofNullable(LOCAL_REPLACEMENTS.getOrDefault(wiki, Map.of()).get(character))
        .or(() -> Optional.ofNullable(REPLACEMENTS.get(character)));
  }

  private static Map<Character, String> createReplacements(final List<CharacterGroup> groups) {
    return groups.stream()
        .flatMap(
            group ->
                group
                    .characters()
                    .chars()
                    .mapToObj(character -> Map.entry((char) character, group.replacement())))
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private record CharacterGroup(String characters, String replacement) {}
}
