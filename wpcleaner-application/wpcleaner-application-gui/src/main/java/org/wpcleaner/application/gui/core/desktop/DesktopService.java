package org.wpcleaner.application.gui.core.desktop;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DesktopService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @SuppressWarnings("SpellCheckingInspection")
  private static final List<String> BROWSERS =
      List.of(
          "x-www-browser",
          "google-chrome",
          "firefox",
          "opera",
          "epiphany",
          "konqueror",
          "conkeror",
          "midori",
          "kazehakase",
          "mozilla");

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public boolean isDesktopSupported() {
    try {
      final Class<?> desktopClass = Class.forName("java.awt.Desktop");
      final Method method = desktopClass.getMethod("isDesktopSupported", (Class<?>[]) null);
      return (Boolean) method.invoke(null, (Object[]) null);
    } catch (Exception e) {
      LOGGER.error(
          "Exception using Desktop.isDesktopSupported(): {} - {}",
          e.getClass().getName(),
          e.getMessage());
    }
    return false;
  }

  public void browse(final String url) {
    try {
      browse(new URI(url), uri -> {});
    } catch (URISyntaxException e) {
      LOGGER.error("Error viewing page {}: {}", url, e.getMessage());
    }
  }

  public void browse(final URI uri, final Consumer<URI> defaultAction) {

    if (browseWithDesktop(uri)) {
      return;
    }

    final String osName = System.getProperty("os.name");
    if (browseByOS(uri, osName)) {
      return;
    }

    for (final String browser : BROWSERS) {
      if (browseWithBrowser(uri, browser, osName)) {
        return;
      }
    }

    defaultAction.accept(uri);
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private boolean browseWithDesktop(final URI uri) {
    if (isDesktopSupported()) {
      try {
        final Class<?> desktopClass = Class.forName("java.awt.Desktop");
        final Method getDesktopMethod = desktopClass.getMethod("getDesktop", (Class<?>[]) null);
        final Object desktop = getDesktopMethod.invoke(null, (Object[]) null);
        final Method browseMethod = desktopClass.getMethod("browse", URI.class);
        browseMethod.invoke(desktop, uri);
        return true;
      } catch (Exception e) {
        LOGGER.error(
            "Exception using Desktop.browse(): {} - {}", e.getClass().getName(), e.getMessage());
      }
    }
    return false;
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private boolean browseByOS(final URI uri, final String osName) {
    // Fallback on OS dependent method, see https://centerkey.com/java/browser/
    if (osName.startsWith("Mac OS")) {
      try {
        Class.forName("com.apple.eio.FileManager")
            .getDeclaredMethod("openURL", String.class)
            .invoke(null, uri.toString());
      } catch (Exception e) {
        logErrorForOS(osName, e);
      }
      return true;
    }

    if (osName.startsWith("Windows")) {
      try {
        Runtime.getRuntime()
            .exec(new String[] {"rundll32", "url.dll,FileProtocolHandler ", uri.toString()});
      } catch (Exception e) {
        logErrorForOS(osName, e);
      }
      return true;
    }

    return false;
  }

  private void logErrorForOS(final String osName, final Exception e) {
    LOGGER.error(
        "Exception on {} for browseUrl(): {} - {}", osName, e.getClass().getName(), e.getMessage());
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private boolean browseWithBrowser(final URI uri, final String browser, final String osName) {
    try {
      if (Runtime.getRuntime().exec(new String[] {"which", browser}).getInputStream().read()
          != -1) {
        Runtime.getRuntime().exec(new String[] {browser, uri.toString()});
        return true;
      }
    } catch (Exception e) {
      LOGGER.error(
          "Exception on {} for browseUrl() with browser {}: {} - {}",
          osName,
          browser,
          e.getClass().getName(),
          e.getMessage());
    }
    LOGGER.error("Unable to find an alternative browser for browseURL() among {}", BROWSERS);
    return false;
  }
}
