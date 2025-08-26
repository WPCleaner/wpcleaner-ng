package org.wpcleaner.api.api.login;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiResponse;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiLogin {

  private final ApiRestClient restClient;

  public ApiLogin(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public Login login(
      final WikiDefinition wiki, final String username, final String password, final String token) {
    return ApiUtils.processApiResponse(
        internalLogin(wiki, username, password, token), Response::login);
  }

  @Nullable
  private Response internalLogin(
      final WikiDefinition wiki, final String username, final String password, final String token) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(LoginParameters.NAME.value, username);
    body.add(LoginParameters.PASSWORD.value, password);
    body.add(LoginParameters.TOKEN.value, token);
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParam(ApiParameters.ACTION.value, ApiParameters.Action.LOGIN.value)
                    .queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value)
                    .queryParam(
                        ApiParameters.FORMAT_VERSION.value, ApiParameters.FORMAT_VERSION_VALUE)
                    .queryParam(
                        ApiParameters.ERROR_FORMAT.value,
                        ApiParameters.ErrorFormat.PLAIN_TEXT.value)
                    .build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("login") Login login)
      implements ApiResponse {}
}
