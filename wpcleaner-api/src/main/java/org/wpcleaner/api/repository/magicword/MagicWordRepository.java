package org.wpcleaner.api.repository.magicword;

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
public class MagicWordRepository implements LogoutHook {

  private final Map<WikiDefinition, List<MagicWord>> magicWords = new HashMap<>();
  private final Lock magicWordsLock = new ReentrantLock();

  public void addMagicWord(final WikiDefinition wiki, final MagicWord magicWord) {
    magicWordsLock.lock();
    try {
      final List<MagicWord> wikiMagicWords =
          magicWords.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiMagicWords.stream()
          .map(MagicWord::name)
          .anyMatch(value -> Objects.equals(value, magicWord.name()))) {
        return;
      }
      wikiMagicWords.add(magicWord);
      wikiMagicWords.sort(Comparator.comparing(MagicWord::name));
    } finally {
      magicWordsLock.unlock();
    }
  }

  public List<MagicWord> getMagicWords(final WikiDefinition wiki) {
    magicWordsLock.lock();
    try {
      return List.copyOf(magicWords.getOrDefault(wiki, List.of()));
    } finally {
      magicWordsLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    magicWordsLock.lock();
    try {
      magicWords.clear();
    } finally {
      magicWordsLock.unlock();
    }
  }
}
