package org.wpcleaner.api.repository.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class TagRepository implements LogoutHook {

  private final Map<WikiDefinition, List<Tag>> tags = new HashMap<>();
  private final Lock tagsLock = new ReentrantLock();

  public void addTag(final WikiDefinition wiki, final Tag tag) {
    tagsLock.lock();
    try {
      final List<Tag> wikiTags = tags.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiTags.stream().map(Tag::name).anyMatch(name -> Objects.equals(name, tag.name()))) {
        return;
      }
      wikiTags.add(tag);
      wikiTags.sort(Comparator.comparing(Tag::name));
    } finally {
      tagsLock.unlock();
    }
  }

  public void addTags(final WikiDefinition wiki, final Collection<Tag> newTags) {
    for (final Tag tag : newTags) {
      addTag(wiki, tag);
    }
  }

  public List<Tag> getTags(final WikiDefinition wiki) {
    tagsLock.lock();
    try {
      return List.copyOf(tags.getOrDefault(wiki, List.of()));
    } finally {
      tagsLock.unlock();
    }
  }

  public Optional<Tag> getSimilarTag(final WikiDefinition wiki, final String name) {
    return getTags(wiki).stream().filter(tag -> name.equalsIgnoreCase(tag.name())).findFirst();
  }

  @Override
  public void logout(final ConnectedUser user) {
    tagsLock.lock();
    try {
      tags.clear();
    } finally {
      tagsLock.unlock();
    }
  }
}
