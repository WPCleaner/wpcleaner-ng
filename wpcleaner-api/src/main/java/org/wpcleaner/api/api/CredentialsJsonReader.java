package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.util.JsonUtil;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;

@Component
public class CredentialsJsonReader implements CredentialsReader {

  private final KnownDefinitions knownDefinitions;

  public CredentialsJsonReader(final KnownDefinitions knownDefinitions) {
    this.knownDefinitions = knownDefinitions;
  }

  @Override
  public List<Credentials> getCredentials() {
    final Resource resource = new ClassPathResource("credentials.json");
    if (!resource.exists() || !resource.isReadable()) {
      return List.of();
    }
    return Arrays.stream(JsonUtil.readValue(resource, JsonCredentials[].class))
        .map(credentials -> credentials.toCredentials(knownDefinitions))
        .filter(Objects::nonNull)
        .toList();
  }

  private record JsonCredentials(
      @JsonProperty(value = "username", required = true) String username,
      @JsonProperty(value = "password", required = true) String password,
      @JsonProperty("wiki") @Nullable String wiki) {

    @Nullable
    public Credentials toCredentials(final KnownDefinitions knownDefinitions) {
      if (wiki == null) {
        return new Credentials(username, password, null);
      }
      return knownDefinitions
          .getDefinition(wiki)
          .map(definition -> new Credentials(username, password, definition))
          .orElseThrow(() -> new IllegalArgumentException("No definition found for " + wiki));
    }
  }
}
