package org.wpcleaner.api.repository.specialpage;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class SpecialPageRepository implements LogoutHook {

  private final Map<WikiDefinition, Map<String, SpecialPage>> specialPages = new HashMap<>();
  private final Lock specialPagesLock = new ReentrantLock();

  public void addSpecialPage(final WikiDefinition wiki, final SpecialPage specialPage) {
    specialPagesLock.lock();
    try {
      specialPages
          .computeIfAbsent(wiki, _ -> new HashMap<>())
          .put(specialPage.realName(), specialPage);
    } finally {
      specialPagesLock.unlock();
    }
  }

  public Optional<SpecialPage> findSpecialPage(final WikiDefinition wiki, final String name) {
    specialPagesLock.lock();
    try {
      return List.copyOf(specialPages.getOrDefault(wiki, Map.of()).values()).stream()
          .filter(specialPage -> specialPage.hasName(name))
          .findFirst();
    } finally {
      specialPagesLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    specialPagesLock.lock();
    try {
      specialPages.clear();
    } finally {
      specialPagesLock.unlock();
    }
  }
}
