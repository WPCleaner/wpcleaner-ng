package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.SpringVersion;
import org.springframework.stereotype.Component;

@Component
public class UserAgent {

  public static final String CLIENT_NAME = "WPCleanerNG";
  public static final String DEFAULT_CONTACT = "https://github.com/WPCleaner/wpcleaner-ng";
  public static final String UNKNOWN_VERSION = "dev";

  private static final String VERSION_RESOURCE = "version.properties";
  private static final String VERSION_PROPERTY = "version";

  private final String client;
  private final String contact;
  private final String libraries;
  private final ObjectProvider<CurrentUserService> currentUserService;

  public UserAgent(
      @Value("${wpcleaner.api.user-agent.contact:" + DEFAULT_CONTACT + "}") final String contact,
      final ObjectProvider<CurrentUserService> currentUserService) {
    this.client = "%s/%s".formatted(CLIENT_NAME, getVersion());
    this.contact = contact;
    this.libraries =
        "spring-web/%s Java/%d"
            .formatted(
                Optional.ofNullable(SpringVersion.getVersion()).orElse(UNKNOWN_VERSION),
                Runtime.version().feature());
    this.currentUserService = currentUserService;
  }

  public static String getVersion() {
    try (InputStream stream = UserAgent.class.getResourceAsStream(VERSION_RESOURCE)) {
      if (stream == null) {
        return UNKNOWN_VERSION;
      }
      final Properties properties = new Properties();
      properties.load(stream);
      return properties.getProperty(VERSION_PROPERTY, UNKNOWN_VERSION);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String getValue() {
    return "%s (%s) %s"
        .formatted(
            client,
            Stream.concat(Stream.of(contact), getUserContact().stream())
                .collect(Collectors.joining("; ")),
            libraries);
  }

  private Optional<String> getUserContact() {
    final ConnectedUser user = currentUserService.getObject().getCurrentUser();
    if (!user.isLoggedIn() || user.demo()) {
      return Optional.empty();
    }
    return Optional.ofNullable(user.wiki().pageUri("User:%s".formatted(user.username())))
        .map(URI::toASCIIString);
  }
}
