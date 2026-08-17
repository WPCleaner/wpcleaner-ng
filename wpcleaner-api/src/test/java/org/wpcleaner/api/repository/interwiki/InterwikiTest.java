package org.wpcleaner.api.repository.interwiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterwikiTest {

  @DisplayName("Should find interwiki by prefix when exists, or return empty optional")
  @Test
  void findInterwiki() {
    // GIVEN
    final Interwiki en =
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/$1", "English", null);
    final Interwiki fr =
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/$1", "French", null);
    final List<Interwiki> interwikis = List.of(en, fr);

    // WHEN
    final Optional<Interwiki> foundEn = Interwiki.findInterwiki(interwikis, "en");
    final Optional<Interwiki> foundFr = Interwiki.findInterwiki(interwikis, "fr");
    final Optional<Interwiki> foundDe = Interwiki.findInterwiki(interwikis, "de");

    // THEN
    Assertions.assertThat(foundEn).hasValue(en);
    Assertions.assertThat(foundFr).hasValue(fr);
    Assertions.assertThat(foundDe).isEmpty();
  }
}
