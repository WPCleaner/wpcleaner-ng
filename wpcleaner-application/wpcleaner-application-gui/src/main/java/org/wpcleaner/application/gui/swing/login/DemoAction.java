package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.Component;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.base.processor.LoginProcessor;
import org.wpcleaner.application.gui.swing.core.worker.SwingWorkerProcessor;

@Service
public class DemoAction {

  private final LoginProcessor loginProcessor;
  private final SwingWorkerProcessor workerProcessor;

  public DemoAction(
      final LoginProcessor loginProcessor, final SwingWorkerProcessor workerProcessor) {
    this.loginProcessor = loginProcessor;
    this.workerProcessor = workerProcessor;
  }

  public void execute(
      final Component component,
      @Nullable final WikiDefinition wiki,
      final String user,
      final BiConsumer<String, WikiDefinition> onSuccess,
      final Consumer<Throwable> onFailure) {
    if (wiki == null) {
      JOptionPane.showMessageDialog(
          component,
          "You must select a wiki before starting demo mode!",
          "Missing wiki",
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (!StringUtils.hasText(user)) {
      JOptionPane.showMessageDialog(
          component,
          "You must input your username before starting demo mode!",
          "Missing username",
          JOptionPane.WARNING_MESSAGE);
      return;
    }
    workerProcessor.process(
        component,
        LoginProcessor.Input.forDemo(wiki, user),
        loginProcessor,
        result -> onSuccess.accept(result.username(), result.wiki()),
        onFailure);
  }
}
