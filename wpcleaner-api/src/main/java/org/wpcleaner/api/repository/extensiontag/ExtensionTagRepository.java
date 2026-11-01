package org.wpcleaner.api.repository.extensiontag;

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
public class ExtensionTagRepository implements LogoutHook {

  private final Map<WikiDefinition, List<ExtensionTag>> extensionTags = new HashMap<>();
  private final Lock extensionTagsLock = new ReentrantLock();

  public void addExtensionTag(final WikiDefinition wiki, final ExtensionTag extensionTag) {
    extensionTagsLock.lock();
    try {
      final List<ExtensionTag> wikiExtensionTags =
          extensionTags.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiExtensionTags.stream()
          .map(ExtensionTag::name)
          .anyMatch(value -> Objects.equals(value, extensionTag.name()))) {
        return;
      }
      wikiExtensionTags.add(extensionTag);
      wikiExtensionTags.sort(Comparator.comparing(ExtensionTag::name));
    } finally {
      extensionTagsLock.unlock();
    }
  }

  public List<ExtensionTag> getExtensionTags(final WikiDefinition wiki) {
    extensionTagsLock.lock();
    try {
      return List.copyOf(extensionTags.getOrDefault(wiki, List.of()));
    } finally {
      extensionTagsLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    extensionTagsLock.lock();
    try {
      extensionTags.clear();
    } finally {
      extensionTagsLock.unlock();
    }
  }
}
