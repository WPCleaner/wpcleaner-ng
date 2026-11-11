package org.wpcleaner.launcher.release;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.wpcleaner.launcher.utils.JsonFiles;

public final class ReleaseRepository {

  public static final String RELEASES_PROPERTY = "wpcleaner.launcher.releases";
  public static final String DEFAULT_RELEASES_URL =
      "https://github.com/WPCleaner/wpcleaner-ng/releases";
  public static final String MANIFEST_NAME = "wpcleaner-ng.json";

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration MANIFEST_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(10);
  private static final int BUFFER_SIZE = 65_536;

  private final String baseUrl;

  public ReleaseRepository(final String releasesUrl) {
    this.baseUrl = releasesUrl;
  }

  public static ReleaseRepository forSystem() {
    return new ReleaseRepository(System.getProperty(RELEASES_PROPERTY, DEFAULT_RELEASES_URL));
  }

  public String releasesUrl() {
    return baseUrl;
  }

  public ReleaseManifest latestManifest() throws IOException {
    final URI uri = URI.create("%s/latest/download/%s".formatted(baseUrl, MANIFEST_NAME));
    try (HttpClient client = createClient()) {
      final HttpResponse<String> response =
          send(client, uri, MANIFEST_TIMEOUT, BodyHandlers.ofString());
      return JsonFiles.read(response.body(), ReleaseManifest.class);
    }
  }

  public void download(
      final ReleaseManifest manifest,
      final ReleaseAsset asset,
      final Path target,
      final DownloadProgress progress)
      throws IOException {
    final URI uri =
        URI.create("%s/download/v%s/%s".formatted(baseUrl, manifest.version(), asset.name()));
    final MessageDigest digest = createDigest();
    try (HttpClient client = createClient()) {
      final HttpResponse<InputStream> response =
          send(client, uri, DOWNLOAD_TIMEOUT, BodyHandlers.ofInputStream());
      final long total = response.headers().firstValueAsLong("Content-Length").orElse(-1);
      try (InputStream input = new DigestInputStream(response.body(), digest);
          OutputStream output = Files.newOutputStream(target)) {
        copy(input, output, total, progress);
      }
    }
    final String checksum = HexFormat.of().formatHex(digest.digest());
    if (!checksum.equals(asset.sha256())) {
      Files.deleteIfExists(target);
      throw new IOException(
          "Checksum of %s is %s instead of %s".formatted(asset.name(), checksum, asset.sha256()));
    }
  }

  private static void copy(
      final InputStream input,
      final OutputStream output,
      final long total,
      final DownloadProgress progress)
      throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    long current = 0;
    int read = input.read(buffer);
    while (read >= 0) {
      output.write(buffer, 0, read);
      current += read;
      progress.downloaded(current, total);
      read = input.read(buffer);
    }
  }

  private static <T> HttpResponse<T> send(
      final HttpClient client,
      final URI uri,
      final Duration timeout,
      final BodyHandler<T> bodyHandler)
      throws IOException {
    final HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
    try {
      final HttpResponse<T> response = client.send(request, bodyHandler);
      if (response.statusCode() != HttpURLConnection.HTTP_OK) {
        throw new IOException("HTTP status %d for %s".formatted(response.statusCode(), uri));
      }
      return response;
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while requesting %s".formatted(uri), e);
    }
  }

  private static HttpClient createClient() {
    return HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  private static MessageDigest createDigest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
