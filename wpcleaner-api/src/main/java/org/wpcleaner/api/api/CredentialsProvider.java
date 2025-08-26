package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Component
public class CredentialsProvider {

  private final List<Credential> credentials;

  public CredentialsProvider(final List<CredentialsReader> readers) {
    credentials = new ArrayList<>();
    readers.stream().map(CredentialsReader::getCredentials).forEach(credentials::addAll);
  }

  public Optional<Credential> getCredential(final WikiDefinition wiki) {
    return getCredentialForWiki(wiki).or(() -> getCredentialForGroup(wiki));
  }

  private Optional<Credential> getCredentialForWiki(final WikiDefinition wiki) {
    return credentials.stream()
        .filter(credential -> Objects.equals(credential.wiki(), wiki))
        .findFirst();
  }

  private Optional<Credential> getCredentialForGroup(final WikiDefinition wiki) {
    if (!wiki.group().isAuthenticationShared()) {
      return Optional.empty();
    }
    return credentials.stream()
        .filter(
            credential ->
                credential.wiki() != null
                    && Objects.equals(credential.wiki().group(), wiki.group()))
        .findFirst();
  }
}
