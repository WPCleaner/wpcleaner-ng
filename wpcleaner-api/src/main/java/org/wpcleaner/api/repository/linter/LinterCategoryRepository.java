package org.wpcleaner.api.repository.linter;

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
public class LinterCategoryRepository implements LogoutHook {

  private final Map<WikiDefinition, List<LinterCategory>> categories = new HashMap<>();
  private final Lock categoriesLock = new ReentrantLock();

  public void setCategories(final WikiDefinition wiki, final List<LinterCategory> wikiCategories) {
    categoriesLock.lock();
    try {
      categories.put(wiki, List.copyOf(wikiCategories));
    } finally {
      categoriesLock.unlock();
    }
  }

  public void setLabels(final WikiDefinition wiki, final Map<String, String> labels) {
    categoriesLock.lock();
    try {
      categories.computeIfPresent(
          wiki,
          (_, wikiCategories) ->
              wikiCategories.stream()
                  .map(category -> category.withLabel(labels.get(category.name())))
                  .toList());
    } finally {
      categoriesLock.unlock();
    }
  }

  public List<LinterCategory> getCategories(final WikiDefinition wiki) {
    categoriesLock.lock();
    try {
      return categories.getOrDefault(wiki, List.of());
    } finally {
      categoriesLock.unlock();
    }
  }

  public boolean hasCategories(final WikiDefinition wiki) {
    categoriesLock.lock();
    try {
      return !categories.getOrDefault(wiki, List.of()).isEmpty();
    } finally {
      categoriesLock.unlock();
    }
  }

  public Optional<LinterCategory> findCategory(final WikiDefinition wiki, final String name) {
    categoriesLock.lock();
    try {
      return categories.getOrDefault(wiki, List.of()).stream()
          .filter(category -> category.name().equals(name))
          .findFirst();
    } finally {
      categoriesLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    categoriesLock.lock();
    try {
      categories.clear();
    } finally {
      categoriesLock.unlock();
    }
  }
}
