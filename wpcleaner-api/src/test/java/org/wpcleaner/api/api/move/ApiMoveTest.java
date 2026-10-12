package org.wpcleaner.api.api.move;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

class ApiMoveTest {

  @DisplayName("ApiMove performs POST request with expected parameters")
  @SuppressWarnings("unchecked")
  @Test
  void testMoveCallsApi() {
    final ApiRestClient apiRestClient = Mockito.mock(ApiRestClient.class);
    final RestClient restClient = Mockito.mock(RestClient.class);
    final RestClient.RequestBodyUriSpec uriSpec = Mockito.mock(RestClient.RequestBodyUriSpec.class);
    final RestClient.RequestBodySpec bodySpec = Mockito.mock(RestClient.RequestBodySpec.class);
    final RestClient.ResponseSpec responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
    final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);

    Mockito.when(apiRestClient.getRestClient(wiki)).thenReturn(restClient);
    Mockito.when(restClient.post()).thenReturn(uriSpec);
    Mockito.when(uriSpec.uri(Mockito.any(Function.class))).thenReturn(bodySpec);
    Mockito.when(bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(bodySpec);

    final ArgumentCaptor<MultiValueMap<String, Object>> bodyCaptor = ArgumentCaptor.captor();
    Mockito.when(bodySpec.body(bodyCaptor.capture())).thenReturn(bodySpec);
    Mockito.when(bodySpec.retrieve()).thenReturn(responseSpec);

    final ApiMove apiMove = new ApiMove(apiRestClient);
    final MoveQueryCommon common =
        MoveQueryCommon.emptyBuilder()
            .to("User:Test/Draft")
            .reason("Drafting page")
            .ignoreWarnings(true)
            .token("test-token")
            .tags(List.of("tag1"))
            .build();
    final MoveQuery query = new MoveQueryByTitle("User:Test", common);

    Assertions.assertThatThrownBy(() -> apiMove.move(wiki, query))
        .hasMessageContaining("No response retrieved");

    final MultiValueMap<String, Object> body = bodyCaptor.getValue();
    Assertions.assertThat(body.getFirst("from")).isEqualTo("User:Test");
    Assertions.assertThat(body.getFirst("to")).isEqualTo("User:Test/Draft");
    Assertions.assertThat(body.getFirst("reason")).isEqualTo("Drafting page");
    Assertions.assertThat(body.getFirst("ignorewarnings")).isEqualTo("1");
    Assertions.assertThat(body.getFirst("tags")).isEqualTo("tag1");
    Assertions.assertThat(body.getFirst("token")).isEqualTo("test-token");
  }

  @DisplayName("ApiMove with MoveQueryByPageId performs POST with fromid")
  @SuppressWarnings("unchecked")
  @Test
  void testMoveByPageId() {
    final ApiRestClient apiRestClient = Mockito.mock(ApiRestClient.class);
    final RestClient restClient = Mockito.mock(RestClient.class);
    final RestClient.RequestBodyUriSpec uriSpec = Mockito.mock(RestClient.RequestBodyUriSpec.class);
    final RestClient.RequestBodySpec bodySpec = Mockito.mock(RestClient.RequestBodySpec.class);
    final RestClient.ResponseSpec responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
    final WikiDefinition wiki = Mockito.mock(WikiDefinition.class);

    Mockito.when(apiRestClient.getRestClient(wiki)).thenReturn(restClient);
    Mockito.when(restClient.post()).thenReturn(uriSpec);
    Mockito.when(uriSpec.uri(Mockito.any(Function.class))).thenReturn(bodySpec);
    Mockito.when(bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(bodySpec);

    final ArgumentCaptor<MultiValueMap<String, Object>> bodyCaptor = ArgumentCaptor.captor();
    Mockito.when(bodySpec.body(bodyCaptor.capture())).thenReturn(bodySpec);
    Mockito.when(bodySpec.retrieve()).thenReturn(responseSpec);

    final ApiMove apiMove = new ApiMove(apiRestClient);
    final MoveQueryCommon common =
        MoveQueryCommon.emptyBuilder().to("User:Test/Draft").token("test-token").build();
    final MoveQuery query = new MoveQueryByPageId(12_345, common);

    Assertions.assertThatThrownBy(() -> apiMove.move(wiki, query))
        .hasMessageContaining("No response retrieved");

    final MultiValueMap<String, Object> body = bodyCaptor.getValue();
    Assertions.assertThat(body.getFirst("fromid")).isEqualTo("12345");
    Assertions.assertThat(body.getFirst("to")).isEqualTo("User:Test/Draft");
  }
}
