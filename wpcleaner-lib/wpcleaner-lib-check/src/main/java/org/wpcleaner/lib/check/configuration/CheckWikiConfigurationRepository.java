package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckWikiConfigurationRepository implements LogoutHook {

  private final Map<WikiDefinition, CheckWikiConfiguration> configurations = new HashMap<>();
  private final Lock configurationsLock = new ReentrantLock();

  public void setConfiguration(
      final WikiDefinition wiki, final CheckWikiConfiguration configuration) {
    configurationsLock.lock();
    try {
      configurations.put(wiki, configuration);
    } finally {
      configurationsLock.unlock();
    }
  }

  public CheckWikiConfiguration getConfiguration(final WikiDefinition wiki) {
    configurationsLock.lock();
    try {
      return configurations.getOrDefault(wiki, CheckWikiConfiguration.EMPTY);
    } finally {
      configurationsLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    configurationsLock.lock();
    try {
      configurations.clear();
    } finally {
      configurationsLock.unlock();
    }
  }
}
