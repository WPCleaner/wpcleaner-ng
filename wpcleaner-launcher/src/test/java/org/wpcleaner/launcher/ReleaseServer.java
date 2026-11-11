package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ReleaseServer implements AutoCloseable {

  static final String ASSET_NAME = "wpcleaner-ng-linux-x64.jar";

  private final HttpServer server;
  private final Map<String, byte[]> contents = new ConcurrentHashMap<>();

  ReleaseServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", this::answer);
    server.start();
  }

  String releasesUrl() {
    return "http://localhost:%d/releases".formatted(server.getAddress().getPort());
  }

  void publish(final String version, final String jarContent) {
    publish(version, jarContent, 25, 1, checksum(jarContent));
  }

  void publish(
      final String version,
      final String jarContent,
      final int javaVersion,
      final int launcherGeneration,
      final String checksum) {
    final String manifest =
        """
        {
          "version": "%s",
          "javaVersion": %d,
          "launcherGeneration": %d,
          "jvmArguments": ["--enable-native-access=ALL-UNNAMED"],
          "assets": {"linux-x64": {"name": "%s", "sha256": "%s"}},
          "futureField": true
        }
        """
            .formatted(version, javaVersion, launcherGeneration, ASSET_NAME, checksum);
    contents.put(
        "/releases/latest/download/wpcleaner-ng.json", manifest.getBytes(StandardCharsets.UTF_8));
    contents.put(
        "/releases/download/v%s/%s".formatted(version, ASSET_NAME),
        jarContent.getBytes(StandardCharsets.UTF_8));
  }

  static String checksum(final String content) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(content.getBytes(StandardCharsets.UTF_8)));
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void close() {
    server.stop(0);
  }

  private void answer(final HttpExchange exchange) throws IOException {
    final byte[] content = contents.get(exchange.getRequestURI().getPath());
    if (content == null) {
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
      exchange.close();
      return;
    }
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, content.length);
    try (OutputStream body = exchange.getResponseBody()) {
      body.write(content);
    }
  }
}
