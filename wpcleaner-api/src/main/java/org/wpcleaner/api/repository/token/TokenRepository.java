package org.wpcleaner.api.repository.token;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class TokenRepository {

  private final ApiTokens apiTokens;

  @Nullable private String csrf;
  private final Lock csrfLock = new ReentrantLock();

  public TokenRepository(final ApiTokens apiTokens) {
    this.apiTokens = apiTokens;
  }

  public String getCsrf(final WikiDefinition wiki) {
    csrfLock.lock();
    try {
      if (csrf != null) {
        return csrf;
      }
      csrf = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.CSRF)).csrf();
      if (csrf == null) {
        throw new ApiException(
            GT._T("Unable to retrieve CSRF token"),
            GT._T("Please check your connection and login status."));
      }
      return csrf;
    } finally {
      csrfLock.unlock();
    }
  }
}
