package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.main.JavaFxMainWindowFactory;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxLoginWindowTest {

  @BeforeAll
  static void setUpClass() {
    JavaFxInitializer.initialize();
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void runOnJavaFx(final Runnable runnable)
      throws InterruptedException, ExecutionException, TimeoutException {
    final CompletableFuture<Void> future = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (final AssertionError | Exception e) {
            future.completeExceptionally(e);
          }
        });
    future.get(5, TimeUnit.SECONDS);
  }

  @DisplayName("JavaFxLoginWindow initializes correctly with close request handler")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services = Mockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = Mockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = Mockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry = Mockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = Mockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = Mockito.mock(KnownDefinitions.class);
          final CredentialsProvider credentialsProvider = Mockito.mock(CredentialsProvider.class);
          final LoginProcessor loginProcessor = Mockito.mock(LoginProcessor.class);
          final JavaFxMainWindowFactory main = Mockito.mock(JavaFxMainWindowFactory.class);
          final UrlService urlService = Mockito.mock(UrlService.class);

          Mockito.when(services.imageLoader()).thenReturn(imageLoader);
          Mockito.when(services.actionServices()).thenReturn(actionServices);
          Mockito.when(services.windowsRegistry()).thenReturn(windowsRegistry);
          Mockito.when(services.apiSiteInfo()).thenReturn(apiSiteInfo);
          Mockito.when(services.knownDefinitions()).thenReturn(knownDefinitions);
          Mockito.when(services.credentialsProvider()).thenReturn(credentialsProvider);
          Mockito.when(services.loginProcessor()).thenReturn(loginProcessor);
          Mockito.when(services.main()).thenReturn(main);
          Mockito.when(services.urlService()).thenReturn(urlService);

          Mockito.when(knownDefinitions.getDefinitions()).thenReturn(Collections.emptyList());
          Mockito.when(credentialsProvider.getCredential(Mockito.any()))
              .thenReturn(Optional.empty());

          final JavaFxLoginWindow window = new JavaFxLoginWindow(services);

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("WPCleaner");
          Assertions.assertThat(window.getName()).isEqualTo("login");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().getOnCloseRequest()).isNotNull();

          window.getStage().close();
        });
  }
}
