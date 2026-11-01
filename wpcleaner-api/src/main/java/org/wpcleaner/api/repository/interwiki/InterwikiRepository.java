package org.wpcleaner.api.repository.interwiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class InterwikiRepository implements LogoutHook {

  private final Map<WikiDefinition, List<Interwiki>> interwikis = new HashMap<>();
  private final Lock interwikisLock = new ReentrantLock();

  public void addInterwiki(final WikiDefinition wiki, final Interwiki interwiki) {
    interwikisLock.lock();
    try {
      final List<Interwiki> wikiInterwikis =
          interwikis.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiInterwikis.stream()
          .map(Interwiki::prefix)
          .anyMatch(prefix -> Objects.equals(prefix, interwiki.prefix()))) {
        return;
      }
      wikiInterwikis.add(interwiki);
      wikiInterwikis.sort(Comparator.comparing(Interwiki::prefix));
    } finally {
      interwikisLock.unlock();
    }
  }

  public List<Interwiki> getInterwikis(final WikiDefinition wiki) {
    interwikisLock.lock();
    try {
      return List.copyOf(interwikis.getOrDefault(wiki, List.of()));
    } finally {
      interwikisLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    interwikisLock.lock();
    try {
      interwikis.clear();
    } finally {
      interwikisLock.unlock();
    }
  }
}
