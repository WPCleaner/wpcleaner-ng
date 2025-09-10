package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.Serial;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.gui.core.action.ActionService;
import org.wpcleaner.application.gui.core.url.UrlService;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;
import org.wpcleaner.application.gui.swing.core.layout.GridBagLayoutService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class SwingLoginWindow extends JFrame {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final transient KnownDefinitions knownDefinitions;
  private final transient ActionService actionService;
  private final transient ComponentService componentService;
  private final transient ImageIconLoader imageService;
  private final transient GridBagLayoutService layoutService;
  private final transient UrlService urlService;

  public static void create(
      final KnownDefinitions knownDefinitions,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    final SwingLoginWindow window =
        new SwingLoginWindow(knownDefinitions, swingCoreServices, urlService);
    window.initialize();
  }

  private SwingLoginWindow(
      final KnownDefinitions knownDefinitions,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    super("WPCleaner");
    this.knownDefinitions = knownDefinitions;
    this.actionService = swingCoreServices.action().action();
    this.componentService = swingCoreServices.component();
    this.imageService = swingCoreServices.image();
    this.layoutService = swingCoreServices.layout();
    this.urlService = urlService;
  }

  private void initialize() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    imageService
        .getImage(ImageCollection.LOGO_WPCLEANER, ImageSize.ICON)
        .map(ImageIcon::getImage)
        .ifPresent(this::setIconImage);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = layoutService.initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    final WikiInput wiki =
        new WikiInput(actionService, componentService, imageService, knownDefinitions);
    addLine(panel, constraints, wiki.label, wiki.icon, wiki.comboBox, wiki.toolBar);

    final LanguageInput language = new LanguageInput(actionService, componentService, imageService);
    addLine(panel, constraints, language.label, language.icon, language.comboBox, language.toolBar);

    final UserInput user = new UserInput(actionService, componentService);
    addLine(panel, constraints, user.label, user.icon, user.comboBox, user.toolBar);

    final PasswordInput password = new PasswordInput(actionService, componentService, urlService);
    addLine(panel, constraints, password.label, password.icon, password.field, password.toolBar);

    addButtons(panel, constraints, user, password);

    layoutService.addFillingPanelBelow(panel);
    getContentPane().add(panel);
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void addLine(
      final JPanel panel,
      final GridBagConstraints constraints,
      final JLabel label,
      final JLabel icon,
      final JComponent selector,
      final JToolBar toolBar) {
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(label),
        GridBagComponent.of(icon),
        GridBagComponent.of(selector, cellConstraints -> cellConstraints.weightx = 1),
        GridBagComponent.of(toolBar));
  }

  private void addButtons(
      final JPanel panel,
      final GridBagConstraints constraints,
      final UserInput userInput,
      final PasswordInput passwordInput) {
    final JPanel buttons = new JPanel(new GridLayout(1, 0));
    final LoginAction loginAction =
        new LoginAction(
            actionService.notImplemented(),
            () -> Objects.requireNonNullElse(userInput.comboBox.getSelectedItem(), "").toString(),
            passwordInput.field::getPassword);
    final JButton loginButton =
        componentService.buttons().builder("Login", true).withAction(loginAction).build();
    buttons.add(loginButton);
    final JButton demoButton =
        componentService
            .buttons()
            .builder("Demo", true)
            .withAction(actionService.notImplemented())
            .build();
    buttons.add(demoButton);
    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            buttons,
            cellConstraints -> cellConstraints.gridwidth = layoutService.getColumnsCount(panel)));

    layoutService.addRow(
        panel,
        constraints,
        GridBagComponent.of(
            componentService.toolBars().feedbacks(),
            cellConstraints -> cellConstraints.gridwidth = layoutService.getColumnsCount(panel)));
  }
}
