package org.wpcleaner.api.repository.interwiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class InterwikiRepository {

  private final List<Interwiki> interwikis = new ArrayList<>();

  public void addInterwiki(final Interwiki interwiki) {
    if (interwikis.stream()
        .map(Interwiki::prefix)
        .anyMatch(prefix -> Objects.equals(prefix, interwiki.prefix()))) {
      return;
    }
    interwikis.add(interwiki);
    interwikis.sort(Comparator.comparing(Interwiki::prefix));
  }

  public List<Interwiki> getInterwikis() {
    return List.copyOf(interwikis);
  }
}
