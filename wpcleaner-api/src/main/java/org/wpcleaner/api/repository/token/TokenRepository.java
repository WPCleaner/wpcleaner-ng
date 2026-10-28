package org.wpcleaner.api.repository.token;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class TokenRepository {

  private final ApiTokens apiTokens;
  private final Map<WikiDefinition, String> csrfTokens = new HashMap<>();
  private final Lock csrfLock = new ReentrantLock();

  public TokenRepository(final ApiTokens apiTokens) {
    this.apiTokens = apiTokens;
  }

  public String getCsrf(final WikiDefinition wiki) {
    csrfLock.lock();
    try {
      final String knownCsrf = csrfTokens.get(wiki);
      if (knownCsrf != null) {
        return knownCsrf;
      }
      final String csrf = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.CSRF)).csrf();
      if (csrf == null) {
        throw new ApiException(
            GT._T("Unable to retrieve CSRF token"),
            GT._T("Please check your connection and login status."));
      }
      csrfTokens.put(wiki, csrf);
      return csrf;
    } finally {
      csrfLock.unlock();
    }
  }

  public void clear() {
    csrfLock.lock();
    try {
      csrfTokens.clear();
    } finally {
      csrfLock.unlock();
    }
  }
}
