package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Component
public class CredentialsYamlReader implements CredentialsReader {

  private final KnownDefinitions knownDefinitions;

  public CredentialsYamlReader(final KnownDefinitions knownDefinitions) {
    this.knownDefinitions = knownDefinitions;
  }

  @Override
  @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
  public List<Credential> getCredentials() {
    final Resource resource = new ClassPathResource("credentials.yaml");
    if (!resource.exists() || !resource.isReadable()) {
      return List.of();
    }
    final Yaml yaml = new Yaml(new Constructor(YamlCredential.class, new LoaderOptions()));
    try {
      return StreamSupport.stream(yaml.loadAll(resource.getInputStream()).spliterator(), false)
          .map(YamlCredential.class::cast)
          .map(credentials -> credentials.toCredential(knownDefinitions))
          .filter(Objects::nonNull)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static final class YamlCredential {

    @Nullable String username;
    @Nullable String password;
    @Nullable String wiki;

    @SuppressWarnings("unused")
    public void setUsername(@Nullable final String username) {
      this.username = username;
    }

    @SuppressWarnings("unused")
    public void setPassword(@Nullable final String password) {
      this.password = password;
    }

    public void setWiki(@Nullable final String wiki) {
      this.wiki = wiki;
    }

    @Nullable
    public Credential toCredential(final KnownDefinitions knownDefinitions) {
      final String localUsername = this.username;
      final String localPassword = this.password;
      if (localUsername == null || localPassword == null) {
        return null;
      }
      if (wiki == null) {
        return new Credential(localUsername, localPassword, null);
      }
      return knownDefinitions
          .getDefinition(wiki)
          .map(definition -> new Credential(localUsername, localPassword, definition))
          .orElseThrow(() -> new IllegalArgumentException("No definition found for " + wiki));
    }
  }
}
