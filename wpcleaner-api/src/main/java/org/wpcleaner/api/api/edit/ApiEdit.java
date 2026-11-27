package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
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
      ApiUtils.addBodyParameter(body, "title", byTitle.title());
    } else if (query instanceof EditQueryByPageId byPageId) {
      ApiUtils.addBodyParameter(body, "pageid", byPageId.pageId());
    }

    final EditQueryCommon common = query.common();
    ApiUtils.addBodyParameter(body, "appendtext", common.appendText());
    ApiUtils.addBodyParameter(body, "basetimestamp", common.baseTimestamp());
    ApiUtils.addBodyParameter(body, "bot", common.bot());
    ApiUtils.addBodyParameter(body, "contentformat", common.contentFormat());
    ApiUtils.addBodyParameter(body, "contentmodel", common.contentModel());
    ApiUtils.addBodyParameter(body, "createonly", common.createOnly());
    ApiUtils.addBodyParameter(body, "md5", common.md5());
    ApiUtils.addBodyParameter(body, "minor", common.minor());
    ApiUtils.addBodyParameter(body, "prependtext", common.prependText());
    ApiUtils.addBodyParameter(body, "nocreate", common.noCreate());
    ApiUtils.addBodyParameter(body, "notminor", common.notMinor());
    ApiUtils.addBodyParameter(body, "recreate", common.recreate());
    ApiUtils.addBodyParameter(body, "redirect", common.redirect());
    ApiUtils.addBodyParameter(body, "section", common.section());
    ApiUtils.addBodyParameter(body, "sectiontitle", common.sectionTitle());
    ApiUtils.addBodyParameter(body, "starttimestamp", common.startTimestamp());
    ApiUtils.addBodyParameter(body, "summary", common.summary());
    ApiUtils.addBodyParameter(body, "tags", common.tags());
    ApiUtils.addBodyParameter(body, "text", common.text());
    ApiUtils.addBodyParameter(body, "undo", common.undo());
    ApiUtils.addBodyParameter(body, "undoafter", common.undoAfter());
    ApiUtils.addBodyParameter(body, "watchlist", common.watchlist());
    ApiUtils.addBodyParameter(body, "token", common.token()); // Must be last
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(uriBuilder -> ApiUriBuilder.of(uriBuilder, ApiParameters.Action.EDIT).build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("edit") Edit edit)
      implements ApiResponse {}
}
