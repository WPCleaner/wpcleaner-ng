package org.wpcleaner.api.rest.lint;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class RestLint {

  public static final String LINT_PATH = "/v1/transform/wikitext/to/lint/{title}";
  public static final String WIKITEXT = "wikitext";

  private final ApiRestClient restClient;

  public RestLint(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<LintError> lint(final WikiDefinition wiki, final String title, final String text) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(WIKITEXT, text);
    final LintError[] errors =
        restClient
            .getRestClient(wiki)
            .post()
            .uri(wiki.restUrl() + LINT_PATH, title.replace(' ', '_'))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(LintError[].class);
    return errors == null ? List.of() : List.of(errors);
  }
}
