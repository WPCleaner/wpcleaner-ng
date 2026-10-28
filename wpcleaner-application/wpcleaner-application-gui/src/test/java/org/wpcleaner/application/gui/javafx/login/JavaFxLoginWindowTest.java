package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.main.JavaFxMainWindowFactory;
import org.wpcleaner.lib.image.ImageLoader;

class JavaFxLoginWindowTest extends JavaFxTest {

  @DisplayName("JavaFxLoginWindow initializes correctly with close request handler")
  @Test
  void testWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final JavaFxLoginWindowServices services =
              BDDMockito.mock(JavaFxLoginWindowServices.class);
          final ImageLoader imageLoader = BDDMockito.mock(ImageLoader.class);
          final JavaFxActionServices actionServices = BDDMockito.mock(JavaFxActionServices.class);
          final JavaFxWindowsRegistry windowsRegistry =
              BDDMockito.mock(JavaFxWindowsRegistry.class);
          final ApiSiteInfo apiSiteInfo = BDDMockito.mock(ApiSiteInfo.class);
          final KnownDefinitions knownDefinitions = BDDMockito.mock(KnownDefinitions.class);
          final CredentialsProvider credentialsProvider =
              BDDMockito.mock(CredentialsProvider.class);
          final LoginProcessor loginProcessor = BDDMockito.mock(LoginProcessor.class);
          final JavaFxMainWindowFactory main = BDDMockito.mock(JavaFxMainWindowFactory.class);
          final UrlService urlService = BDDMockito.mock(UrlService.class);

          BDDMockito.given(services.imageLoader()).willReturn(imageLoader);
          BDDMockito.given(services.actionServices()).willReturn(actionServices);
          BDDMockito.given(services.windowsRegistry()).willReturn(windowsRegistry);
          BDDMockito.given(services.apiSiteInfo()).willReturn(apiSiteInfo);
          BDDMockito.given(services.knownDefinitions()).willReturn(knownDefinitions);
          BDDMockito.given(services.credentialsProvider()).willReturn(credentialsProvider);
          BDDMockito.given(services.loginProcessor()).willReturn(loginProcessor);
          BDDMockito.given(services.main()).willReturn(main);
          BDDMockito.given(services.urlService()).willReturn(urlService);

          BDDMockito.given(knownDefinitions.getDefinitions()).willReturn(Collections.emptyList());
          BDDMockito.given(credentialsProvider.getCredential(BDDMockito.any()))
              .willReturn(Optional.empty());

          final JavaFxLoginWindow window = new JavaFxLoginWindow(services);

          Assertions.assertThat(window.getStage().getTitle()).isEqualTo("WPCleaner");
          Assertions.assertThat(window.getName()).isEqualTo("login");
          Assertions.assertThat(window.getHelpPage()).isEqualTo("Login");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().getOnCloseRequest()).isNotNull();

          final KeyEvent f1 =
              new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false, false, false);
          window.getStage().getScene().getRoot().fireEvent(f1);
          BDDMockito.then(actionServices).should().displayHelp(window);

          window.getStage().close();
        });
  }
}
