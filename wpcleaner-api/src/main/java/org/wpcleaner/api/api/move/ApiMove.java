package org.wpcleaner.api.api.move;

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
      addParameter(body, MoveParameters.FROM.value, byTitle.from());
    } else if (query instanceof MoveQueryByPageId byPageId) {
      addParameter(body, MoveParameters.FROM_ID.value, byPageId.fromId());
    }

    final MoveQueryCommon common = query.common();
    addParameter(body, MoveParameters.TO.value, common.to());
    addParameter(body, MoveParameters.REASON.value, common.reason());
    addParameter(body, MoveParameters.MOVE_TALK.value, common.moveTalk());
    addParameter(body, MoveParameters.MOVE_SUBPAGES.value, common.moveSubpages());
    addParameter(body, MoveParameters.NO_REDIRECT.value, common.noRedirect());
    addParameter(body, MoveParameters.WATCHLIST.value, common.watchlist());
    addParameter(body, MoveParameters.WATCHLIST_EXPIRY.value, common.watchlistExpiry());
    addParameter(body, MoveParameters.IGNORE_WARNINGS.value, common.ignoreWarnings());
    addParameter(body, MoveParameters.TAGS.value, common.tags());
    addParameter(body, MoveParameters.TOKEN.value, common.token()); // Must be last
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(uriBuilder -> ApiUriBuilder.of(uriBuilder, ApiParameters.Action.MOVE).build())
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
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("move") Move move,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}
}
