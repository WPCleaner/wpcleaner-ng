package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;

@SuppressWarnings("PMD.DataClass")
public record WikiWarning(String text) {

  public static final WikiWarning BENGALI = new WikiWarning(languageWarning("Bengali", null));
  public static final WikiWarning GOYCHI_KONKNNI =
      new WikiWarning(languageWarning("Gõychi Konknni", null));
  public static final WikiWarning HINDI = new WikiWarning(languageWarning("Hindi", null));
  public static final WikiWarning KANNADA = new WikiWarning(languageWarning("Kannada", null));
  public static final WikiWarning KOREAN = new WikiWarning(languageWarning("Korean", null));
  public static final WikiWarning MANIPURI = new WikiWarning(languageWarning("Manipuri", null));
  public static final WikiWarning MALAYALAM = new WikiWarning(languageWarning("Malayalam", null));
  public static final WikiWarning PUNJABI = new WikiWarning(languageWarning("Punjabi", null));
  public static final WikiWarning TAMIL = new WikiWarning(languageWarning("Tamil", null));
  public static final WikiWarning TELUGU =
      new WikiWarning(
          languageWarning("Telugu", "Example of fonts for Telugu: Gautami, Noto Sans Telugu."));

  private static String languageWarning(
      final String language, @Nullable final String extraWarning) {
    final String baseMessage =
            """
                %s language usually requires a dedicated font to be displayed, instead of square boxes.
                Don't forget to install such a font, and configure WPCleaner to use it.
                Even with such a font, rendering may still be incorrect, due to Java limitations.
                """
            .formatted(language);
    if (extraWarning == null) {
      return baseMessage;
    }
    return baseMessage + "\n" + extraWarning;
  }
}
