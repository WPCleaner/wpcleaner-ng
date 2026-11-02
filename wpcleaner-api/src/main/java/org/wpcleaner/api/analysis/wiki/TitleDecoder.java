package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;

final class TitleDecoder {

  private static final char PERCENT = '%';
  private static final int HEX_LENGTH = 2;
  private static final Pattern INVALID_TITLE_CHARACTERS = Pattern.compile("[<>\\[\\]{}|]");

  private final PageTitleNormalizer pageTitleNormalizer;

  TitleDecoder(final PageTitleNormalizer pageTitleNormalizer) {
    this.pageTitleNormalizer = pageTitleNormalizer;
  }

  Optional<PageTitle> decode(final String name, final int defaultNamespace) {
    final String decodedName = name.indexOf(PERCENT) < 0 ? name : decodePercent(name);
    if (INVALID_TITLE_CHARACTERS.matcher(decodedName).find()) {
      return Optional.empty();
    }
    return pageTitleNormalizer.normalize(decodedName, defaultNamespace);
  }

  private static String decodePercent(final String value) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final StringBuilder decoded = new StringBuilder(value.length());
    int index = 0;
    while (index < value.length()) {
      if (value.charAt(index) == PERCENT
          && index + HEX_LENGTH < value.length()
          && HexFormat.isHexDigit(value.charAt(index + 1))
          && HexFormat.isHexDigit(value.charAt(index + HEX_LENGTH))) {
        bytes.write(HexFormat.fromHexDigits(value, index + 1, index + HEX_LENGTH + 1));
        index += HEX_LENGTH + 1;
      } else {
        decoded.append(bytes.toString(StandardCharsets.UTF_8));
        bytes.reset();
        decoded.append(value.charAt(index));
        index++;
      }
    }
    return decoded.append(bytes.toString(StandardCharsets.UTF_8)).toString();
  }
}
