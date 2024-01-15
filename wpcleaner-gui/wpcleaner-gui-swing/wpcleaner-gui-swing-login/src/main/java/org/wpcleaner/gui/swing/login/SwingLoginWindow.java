package org.wpcleaner.gui.swing.login;

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
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.SwingCoreServices;
import org.wpcleaner.gui.swing.core.component.ComponentService;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.gui.swing.core.layout.GridBagLayoutService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class SwingLoginWindow extends JFrame {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final KnownDefinitions knownDefinitions;
  private final ActionService actionService;
  private final ComponentService componentService;
  private final ImageIconLoader imageService;
  private final GridBagLayoutService layoutService;

  public static void create(
      final KnownDefinitions knownDefinitions, final SwingCoreServices swingCoreServices) {
    final SwingLoginWindow window = new SwingLoginWindow(knownDefinitions, swingCoreServices);
    window.initialize();
  }

  private SwingLoginWindow(
      final KnownDefinitions knownDefinitions, final SwingCoreServices swingCoreServices) {
    super("WPCleaner");
    this.knownDefinitions = knownDefinitions;
    this.actionService = swingCoreServices.action().action();
    this.componentService = swingCoreServices.component();
    this.imageService = swingCoreServices.image();
    this.layoutService = swingCoreServices.layout();
  }

  private void initialize() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    final PasswordInput password = new PasswordInput(actionService, componentService);
    addLine(panel, constraints, password.label, password.icon, password.field, password.toolBar);

    addButtons(panel, constraints, user, password);

    layoutService.addFillingPanelBelow(panel);
    getContentPane().add(panel);
    pack();
    setVisible(true);
  }

  private void addLine(
      final JPanel panel,
      final GridBagConstraints constraints,
      final JLabel label,
      final JLabel icon,
      final JComponent selector,
      final JToolBar toolBar) {
    constraints.gridx = 0;
    panel.add(label, constraints);
    constraints.gridx++;
    panel.add(icon, constraints);
    constraints.gridx++;
    constraints.weightx = 1;
    panel.add(selector, constraints);
    constraints.gridx++;
    constraints.weightx = 0;
    panel.add(toolBar, constraints);
    constraints.gridy++;
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
    constraints.gridx = 0;
    constraints.gridwidth = layoutService.getColumnsCount(panel);
    panel.add(buttons, constraints);
    constraints.gridy++;

    final JButton feedbackButton =
        componentService
            .buttons()
            .builder("Feedback", false)
            .withIcon(ImageCollection.HELP_FAQ, ImageSize.BUTTON)
            .withAction(new FeedbackAction(componentService.menuItems()))
            .build();
    final JToolBar buttonTooBar =
        componentService
            .toolBars()
            .builder()
            .withComponent(feedbackButton)
            .withComponent(componentService.buttons().options())
            .withComponent(componentService.buttons().about())
            .build();
    constraints.gridx = 0;
    constraints.gridwidth = layoutService.getColumnsCount(panel);
    panel.add(buttonTooBar, constraints);
    constraints.gridy++;
  }
}
