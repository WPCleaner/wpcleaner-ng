package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import org.springframework.util.StringUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.swing.core.action.ComponentAction;

public record LoginAction(
    Supplier<Optional<WikiDefinition>> wikiSupplier,
    Supplier<String> userSupplier,
    Supplier<char[]> passwordSupplier,
    BiConsumer<String, WikiDefinition> postLoginAction)
    implements ComponentAction {

  @Override
  public void execute(final Component component) {
    final Optional<WikiDefinition> selectedWiki = wikiSupplier.get();
    if (selectedWiki.isEmpty()) {
      JOptionPane.showMessageDialog(
          null,
          "You must select a wiki before login!",
          "Missing wiki",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    final String user = userSupplier().get();
    if (!StringUtils.hasText(user)) {
      JOptionPane.showMessageDialog(
          null,
          "You must input your username before login!",
          "Missing username",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    final char[] password = passwordSupplier().get();
    if (password == null || password.length == 0) {
      JOptionPane.showMessageDialog(
          null,
          "You must input your password before login!",
          "Missing password",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    // TODO: Login

    postLoginAction.accept(user, selectedWiki.get());
  }
}
