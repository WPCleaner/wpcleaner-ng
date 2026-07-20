package org.wpcleaner.api.repository.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.list.tags.Tag;

@Service
public class TagRepository {

  private final List<Tag> tags = new ArrayList<>();

  public void addTag(final Tag tag) {
    if (tags.stream().map(Tag::name).anyMatch(name -> Objects.equals(name, tag.name()))) {
      return;
    }
    tags.add(tag);
    tags.sort(Comparator.comparing(Tag::name));
  }

  public void addTags(final Collection<Tag> newTags) {
    for (final Tag tag : newTags) {
      addTag(tag);
    }
  }

  public List<Tag> getTags() {
    return List.copyOf(tags);
  }
}
