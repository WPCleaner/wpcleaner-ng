package org.wpcleaner.launcher.release;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public record ReleaseVersion(List<Integer> numbers) implements Comparable<ReleaseVersion> {

  private static final Pattern FORMAT = Pattern.compile("\\d{1,9}(\\.\\d{1,9}){0,5}");

  public ReleaseVersion {
    numbers = List.copyOf(numbers);
  }

  public static ReleaseVersion parse(final String text) {
    if (!FORMAT.matcher(text).matches()) {
      throw new IllegalArgumentException("Invalid version: %s".formatted(text));
    }
    return new ReleaseVersion(Arrays.stream(text.split("\\.")).map(Integer::valueOf).toList());
  }

  @Override
  public int compareTo(final ReleaseVersion other) {
    final int size = Math.max(numbers.size(), other.numbers.size());
    for (int index = 0; index < size; index++) {
      final int comparison = Integer.compare(number(index), other.number(index));
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }

  public boolean isNewerThan(final ReleaseVersion other) {
    return compareTo(other) > 0;
  }

  private int number(final int index) {
    return index < numbers.size() ? numbers.get(index) : 0;
  }

  @Override
  public String toString() {
    return String.join(".", numbers.stream().map(String::valueOf).toList());
  }
}
