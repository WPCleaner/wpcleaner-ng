package org.wpcleaner.api.repository.magiclink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class MagicLinkRepository implements LogoutHook {

  private final Map<WikiDefinition, Set<MagicLinkType>> magicLinks = new HashMap<>();
  private final Lock magicLinksLock = new ReentrantLock();

  public void enableMagicLink(final WikiDefinition wiki, final MagicLinkType magicLink) {
    magicLinksLock.lock();
    try {
      magicLinks.computeIfAbsent(wiki, _ -> EnumSet.noneOf(MagicLinkType.class)).add(magicLink);
    } finally {
      magicLinksLock.unlock();
    }
  }

  public Set<MagicLinkType> getMagicLinks(final WikiDefinition wiki) {
    magicLinksLock.lock();
    try {
      return Set.copyOf(magicLinks.getOrDefault(wiki, Set.of()));
    } finally {
      magicLinksLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    magicLinksLock.lock();
    try {
      magicLinks.clear();
    } finally {
      magicLinksLock.unlock();
    }
  }
}
