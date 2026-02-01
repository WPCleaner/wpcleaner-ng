package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.Serial;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;
import org.wpcleaner.application.gui.swing.core.window.WPCleanerWindow;

public final class SwingLoginWindow extends WPCleanerWindow<SwingLoginWindowServices> {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  public static void create(final SwingLoginWindowServices services) {
    final SwingLoginWindow window = new SwingLoginWindow(services);
    window.initialize();
  }

  private SwingLoginWindow(final SwingLoginWindowServices services) {
    super(services);
  }

  @Override
  protected void initialize() {
    super.initialize();
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = services.swing().layout().initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    final WikiInput wiki = new WikiInput(services.swing(), services.knownDefinitions());
    addLine(panel, constraints, wiki.label, wiki.icon, wiki.comboBox, wiki.toolBar);

    final LanguageInput language = new LanguageInput(services.swing());
    addLine(panel, constraints, language.label, language.icon, language.comboBox, language.toolBar);

    final UserInput user = new UserInput(services.swing());
    addLine(panel, constraints, user.label, user.icon, user.comboBox, user.toolBar);

    final PasswordInput password = new PasswordInput(services.swing(), services.url());
    addLine(panel, constraints, password.label, password.icon, password.field, password.toolBar);

    addButtons(panel, constraints, wiki, user, password);

    services.swing().layout().addFillingPanelBelow(panel);
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
    services
        .swing()
        .layout()
        .addRow(
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
      final WikiInput wikiInput,
      final UserInput userInput,
      final PasswordInput passwordInput) {
    final JPanel buttons = new JPanel(new GridLayout(1, 0));
    final JButton loginButton =
        services
            .swing()
            .component()
            .buttons()
            .builder("Login", true)
            .withAction(
                component ->
                    services
                        .login()
                        .execute(
                            component,
                            wikiInput.getSelectedWiki().orElse(null),
                            userInput.getUser(),
                            passwordInput.getPassword(),
                            this::displayMainWindow,
                            e -> services.swing().errorDialog().showErrorMessage(this, e)))
            .build();
    buttons.add(loginButton);
    final JButton demoButton =
        services
            .swing()
            .component()
            .buttons()
            .builder("Demo", true)
            .withAction(
                component ->
                    services
                        .demo()
                        .execute(
                            component,
                            wikiInput.getSelectedWiki().orElse(null),
                            userInput.getUser(),
                            this::displayMainWindow,
                            e -> services.swing().errorDialog().showErrorMessage(this, e)))
            .build();
    buttons.add(demoButton);
    services.swing().layout().addRowSpanningAllColumns(panel, constraints, buttons);

    services
        .swing()
        .layout()
        .addRowSpanningAllColumns(
            panel, constraints, services.swing().component().toolBars().feedbacks());
  }

  private void displayMainWindow() {
    services.main().displayMainWindow();
    this.dispose();
  }
}
