package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.builder.WikipediaBuilder;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@SuppressWarnings({"PMD.DataClass", "SpellCheckingInspection"})
public class WikipediaDefinitions implements WikiDefinitions {
  public static final WikiDefinition AF = ltr("af", "Afrikaans Wikipedia");
  public static final WikiDefinition ALS = ltr("als", "Alemannisch Wikipedia");
  public static final WikiDefinition AR = rtl("ar", "Arabic Wikipedia");
  public static final WikiDefinition ARY = rtl("ary", "Moroccan Arabic Wikipedia");
  public static final WikiDefinition ARZ = rtl("arz", "Egyptian Arabic Wikipedia");
  public static final WikiDefinition AST = ltr("ast", "Wikipedia n'asturianu");
  public static final WikiDefinition BAR = ltr("bar", "Boarich Wikipedia");
  public static final WikiDefinition BE = ltr("be", "Беларускай Вікіпедыяй");
  public static final WikiDefinition BE_TARASK = ltr("be-tarask", "Беларуская Вікіпэдыя");
  public static final WikiDefinition BG = ltr("bg", "Българоезична Уикипедия");
  public static final WikiDefinition BN = ltr("bn", "Bengali Wikipedia", WikiWarning.BENGALI);
  public static final WikiDefinition BS = ltr("bs", "Wikipedia na bosanskom jeziku");
  public static final WikiDefinition CA = ltr("ca", "Viquipèdia en català");
  public static final WikiDefinition CKB = ltr("ckb", "Kurdish Sorani Wikipedia");
  public static final WikiDefinition CS = ltr("cs", "Czech Wikipedia");
  public static final WikiDefinition CY = ltr("cy", "Welsh Wikipedia");
  public static final WikiDefinition DA = ltr("da", "Danish Wikipedia");
  public static final WikiDefinition DE = ltr("de", "Deutschsprachige Wikipedia");
  public static final WikiDefinition EL = ltr("el", "Greek Wikipedia");
  public static final WikiDefinition EN = ltr("en", "English Wikipedia");
  public static final WikiDefinition EO = ltr("eo", "Esperanto Wikipedia");
  public static final WikiDefinition ES = ltr("es", "Wikipedia en español");
  public static final WikiDefinition FA = rtl("fa", "Persian Wikipedia");
  public static final WikiDefinition FI = ltr("fi", "Finnish Wikipedia");
  public static final WikiDefinition FR = ltr("fr", "Wikipédia en Français");
  public static final WikiDefinition FY = ltr("fy", "West Frisian Wikipedia");
  public static final WikiDefinition GD = ltr("gd", "Scottish Gaelic Wikipedia");
  public static final WikiDefinition GL = ltr("gl", "Galipedia");
  public static final WikiDefinition GOM =
      ltr("gom", "कोंकणी विकिपीडिया / Konknni Wikipedia", WikiWarning.GOYCHI_KONKNNI);
  public static final WikiDefinition HE = rtl("he", "ויקיפדיה העברית");
  public static final WikiDefinition HI = ltr("hi", "Hindi Wikipedia", WikiWarning.HINDI);
  public static final WikiDefinition HIF = ltr("hif", "Fiji Hindi Wikipedia");
  public static final WikiDefinition HR = ltr("hr", "Wikipedija na hrvatskom jeziku");
  public static final WikiDefinition HU = ltr("hu", "Magyar Wikipedia");
  public static final WikiDefinition ID = ltr("id", "Indonesian Wikipedia");
  public static final WikiDefinition IS = ltr("is", "Wikipedia á íslensku");
  public static final WikiDefinition IT = ltr("it", "Italian Wikipedia");
  public static final WikiDefinition JA = ltr("ja", "Japanese Wikipedia");
  public static final WikiDefinition KN = ltr("kn", "Kannada Wikipedia", WikiWarning.KANNADA);
  public static final WikiDefinition KO = ltr("ko", "Korean Wikipedia", WikiWarning.KOREAN);
  public static final WikiDefinition LA = ltr("la", "Latin Wikipedia");
  public static final WikiDefinition LI = ltr("li", "Limburgish Wikipedia");
  public static final WikiDefinition LV = ltr("lv", "Latvian Wikipedia");
  public static final WikiDefinition ML = ltr("ml", "Malayalam Wikipedia", WikiWarning.MALAYALAM);
  public static final WikiDefinition MNI = ltr("mni", "Meitei Wikipedia", WikiWarning.MANIPURI);
  public static final WikiDefinition NDS = ltr("nds", "Low Saxon Wikipedia");
  public static final WikiDefinition NDS_NL = ltr("nds-nl", "Dutch Low Saxon Wikipedia");
  public static final WikiDefinition NL = ltr("nl", "Nederlandstalige Wikipedia");
  public static final WikiDefinition NO = ltr("no", "Norsk Wikipedia på bokmål og riksmål");
  public static final WikiDefinition PA = ltr("pa", "ਪੰਜਾਬੀ ਵਿਕੀਪੀਡੀਆ", WikiWarning.PUNJABI);
  public static final WikiDefinition PDC = ltr("pdc", "Pennsylvania German Wikipedia");
  public static final WikiDefinition PL = ltr("pl", "Polska Wikipedia");
  public static final WikiDefinition PT = ltr("pt", "Wikipédia em português");
  public static final WikiDefinition RO = ltr("ro", "Romanian Wikipedia");
  public static final WikiDefinition RU = ltr("ru", "Русская Википедия");
  public static final WikiDefinition SCO = ltr("sco", "Scots wikipedia");
  public static final WikiDefinition SIMPLE = ltr("simple", "Simple English Wikipedia");
  public static final WikiDefinition SK = ltr("sk", "Slovak Wikipedia");
  public static final WikiDefinition SL = ltr("sl", "Slovenska Wikipedija");
  public static final WikiDefinition SQ = ltr("sq", "Albanian Wikipedia");
  public static final WikiDefinition SR = ltr("sr", "Википедија на српском језику");
  public static final WikiDefinition SV = ltr("sv", "Swedish Wikipedia");
  public static final WikiDefinition TA = ltr("ta", "தமிழ் விக்கிப்பீடியா", WikiWarning.TAMIL);
  public static final WikiDefinition TE = ltr("te", "తెలుగు వికీపీడియా", WikiWarning.TELUGU);
  public static final WikiDefinition TR = ltr("tr", "Turkish Wikipedia");
  public static final WikiDefinition UK = ltr("uk", "Ukrainian Wikipedia");
  public static final WikiDefinition UR = rtl("ur", "Urdu Wikipedia");
  public static final WikiDefinition UZ = ltr("uz", "Uzbek Wikipedia");
  public static final WikiDefinition VEC = ltr("vec", "Wikipedia Vèneta");
  public static final WikiDefinition VI = ltr("vi", "Vietnamese Wikipedia");
  public static final WikiDefinition YI = rtl("yi", "Yiddish Wikipedia");
  public static final WikiDefinition ZH = ltr("zh", "维基百科");

  private static WikiDefinition ltr(final String language, final String name) {
    return WikipediaBuilder.ltr(language, name);
  }

  private static WikiDefinition ltr(
      final String language, final String name, final WikiWarning warning) {
    return WikipediaBuilder.ltr(language, name, warning);
  }

  private static WikiDefinition rtl(final String language, final String name) {
    return WikipediaBuilder.rtl(language, name);
  }
}
