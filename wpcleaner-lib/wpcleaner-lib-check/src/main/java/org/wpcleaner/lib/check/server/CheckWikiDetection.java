package org.wpcleaner.lib.check.server;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CheckWikiDetection(
    int errorNumber, int location, String notice, boolean whitelisted) {

  public static final int UNKNOWN_LOCATION = -1;

  private static final Pattern DETECTION =
      Pattern.compile("^\\+\\s+(\\d+)\\s+(-?\\d+)(?:\\s+(.*))?$");
  private static final Pattern WHITELISTED =
      Pattern.compile("^.* is in whitelist with error: (\\d+)$");
  private static final int ERROR_NUMBER_GROUP = 1;
  private static final int LOCATION_GROUP = 2;
  private static final int NOTICE_GROUP = 3;

  static Optional<CheckWikiDetection> parse(final String line) {
    final String trimmedLine = line.strip();
    final Matcher whitelisted = WHITELISTED.matcher(trimmedLine);
    if (whitelisted.matches()) {
      return Optional.of(
          new CheckWikiDetection(
              Integer.parseInt(whitelisted.group(ERROR_NUMBER_GROUP)), UNKNOWN_LOCATION, "", true));
    }
    final Matcher detection = DETECTION.matcher(trimmedLine);
    if (!detection.matches()) {
      return Optional.empty();
    }
    final String notice = detection.group(NOTICE_GROUP);
    return Optional.of(
        new CheckWikiDetection(
            Integer.parseInt(detection.group(ERROR_NUMBER_GROUP)),
            Integer.parseInt(detection.group(LOCATION_GROUP)),
            notice == null ? "" : notice,
            false));
  }
}
