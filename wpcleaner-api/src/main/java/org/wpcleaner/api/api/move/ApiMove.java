package org.wpcleaner.api.api.move;

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
public class ApiMove {

  private final ApiRestClient restClient;

  public ApiMove(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public Move move(final WikiDefinition wiki, final MoveQuery query) {
    return ApiUtils.processApiResponse(internalMove(wiki, query), Response::move);
  }

  @Nullable
  private Response internalMove(final WikiDefinition wiki, final MoveQuery query) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    if (query instanceof MoveQueryByTitle byTitle) {
      ApiUtils.addBodyParameter(body, MoveParameters.FROM.value, byTitle.from());
    } else if (query instanceof MoveQueryByPageId byPageId) {
      ApiUtils.addBodyParameter(body, MoveParameters.FROM_ID.value, byPageId.fromId());
    }

    final MoveQueryCommon common = query.common();
    ApiUtils.addBodyParameter(body, MoveParameters.TO.value, common.to());
    ApiUtils.addBodyParameter(body, MoveParameters.REASON.value, common.reason());
    ApiUtils.addBodyParameter(body, MoveParameters.MOVE_TALK.value, common.moveTalk());
    ApiUtils.addBodyParameter(body, MoveParameters.MOVE_SUBPAGES.value, common.moveSubpages());
    ApiUtils.addBodyParameter(body, MoveParameters.NO_REDIRECT.value, common.noRedirect());
    ApiUtils.addBodyParameter(body, MoveParameters.WATCHLIST.value, common.watchlist());
    ApiUtils.addBodyParameter(
        body, MoveParameters.WATCHLIST_EXPIRY.value, common.watchlistExpiry());
    ApiUtils.addBodyParameter(body, MoveParameters.IGNORE_WARNINGS.value, common.ignoreWarnings());
    ApiUtils.addBodyParameter(body, MoveParameters.TAGS.value, common.tags());
    ApiUtils.addBodyParameter(body, MoveParameters.TOKEN.value, common.token()); // Must be last
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(uriBuilder -> ApiUriBuilder.of(uriBuilder, ApiParameters.Action.MOVE).build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("move") Move move,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}
}
