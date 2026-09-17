package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiResponse;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiEdit {

  private final ApiRestClient restClient;

  public ApiEdit(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public Edit edit(final WikiDefinition wiki, final EditQuery query) {
    return ApiUtils.processApiResponse(internalEdit(wiki, query), Response::edit);
  }

  @Nullable
  private Response internalEdit(final WikiDefinition wiki, final EditQuery query) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    if (query instanceof EditQueryByTitle byTitle) {
      addParameter(body, "title", byTitle.title());
    } else if (query instanceof EditQueryByPageId byPageId) {
      addParameter(body, "pageid", byPageId.pageId());
    }

    final EditQueryCommon common = query.common();
    addParameter(body, "appendtext", common.appendText());
    addParameter(body, "basetimestamp", common.baseTimestamp());
    addParameter(body, "bot", common.bot());
    addParameter(body, "contentformat", common.contentFormat());
    addParameter(body, "contentmodel", common.contentModel());
    addParameter(body, "createonly", common.createOnly());
    addParameter(body, "md5", common.md5());
    addParameter(body, "minor", common.minor());
    addParameter(body, "prependtext", common.prependText());
    addParameter(body, "nocreate", common.noCreate());
    addParameter(body, "notminor", common.notMinor());
    addParameter(body, "recreate", common.recreate());
    addParameter(body, "redirect", common.redirect());
    addParameter(body, "section", common.section());
    addParameter(body, "sectiontitle", common.sectionTitle());
    addParameter(body, "starttimestamp", common.startTimestamp());
    addParameter(body, "summary", common.summary());
    addParameter(body, "tags", common.tags());
    addParameter(body, "text", common.text());
    addParameter(body, "undo", common.undo());
    addParameter(body, "undoafter", common.undoAfter());
    addParameter(body, "watchlist", common.watchlist());
    addParameter(body, "token", common.token()); // Must be last
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(uriBuilder -> ApiUriBuilder.of(uriBuilder, ApiParameters.Action.EDIT).build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private void addParameter(
      final MultiValueMap<String, Object> body, final String key, @Nullable final Object value) {
    switch (value) {
      case null -> {}
      case Boolean _ -> {
        if (Boolean.TRUE.equals(value)) {
          body.add(key, "1");
        }
      }
      case List<?> list -> {
        if (!list.isEmpty()) {
          body.add(key, list.stream().map(Object::toString).collect(Collectors.joining("|")));
        }
      }
      default -> body.add(key, value.toString());
    }
  }

  private record Response(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("edit") Edit edit)
      implements ApiResponse {}
}
