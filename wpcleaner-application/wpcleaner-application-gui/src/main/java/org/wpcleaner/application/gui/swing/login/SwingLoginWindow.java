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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.layout.GridBagComponent;

public final class SwingLoginWindow extends JFrame {

  @Serial private static final long serialVersionUID = 3951316694154990744L;

  private final transient DemoAction demoAction;
  private final transient KnownDefinitions knownDefinitions;
  private final transient LoginAction loginAction;
  private final transient MainWindowFactory mainWindowFactory;
  private final transient SwingCoreServices swingCoreServices;
  private final transient UrlService urlService;

  public static void create(
      final DemoAction demoAction,
      final KnownDefinitions knownDefinitions,
      final LoginAction loginAction,
      final MainWindowFactory mainWindowFactory,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    final SwingLoginWindow window =
        new SwingLoginWindow(
            demoAction,
            knownDefinitions,
            loginAction,
            mainWindowFactory,
            swingCoreServices,
            urlService);
    window.initialize();
  }

  private SwingLoginWindow(
      final DemoAction demoAction,
      final KnownDefinitions knownDefinitions,
      final LoginAction loginAction,
      final MainWindowFactory mainWindowFactory,
      final SwingCoreServices swingCoreServices,
      final UrlService urlService) {
    super("WPCleaner");
    this.demoAction = demoAction;
    this.knownDefinitions = knownDefinitions;
    this.loginAction = loginAction;
    this.mainWindowFactory = mainWindowFactory;
    this.swingCoreServices = swingCoreServices;
    this.urlService = urlService;
  }

  private void initialize() {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    swingCoreServices.image().setIconImage(this);
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = swingCoreServices.layout().initializeConstraints();
    constraints.fill = GridBagConstraints.BOTH;

    final WikiInput wiki = new WikiInput(swingCoreServices, knownDefinitions);
    addLine(panel, constraints, wiki.label, wiki.icon, wiki.comboBox, wiki.toolBar);

    final LanguageInput language = new LanguageInput(swingCoreServices);
    addLine(panel, constraints, language.label, language.icon, language.comboBox, language.toolBar);

    final UserInput user = new UserInput(swingCoreServices);
    addLine(panel, constraints, user.label, user.icon, user.comboBox, user.toolBar);

    final PasswordInput password = new PasswordInput(swingCoreServices, urlService);
    addLine(panel, constraints, password.label, password.icon, password.field, password.toolBar);

    addButtons(panel, constraints, wiki, user, password);

    swingCoreServices.layout().addFillingPanelBelow(panel);
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
    swingCoreServices
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
        swingCoreServices
            .component()
            .buttons()
            .builder("Login", true)
            .withAction(
                component ->
                    loginAction.execute(
                        component,
                        wikiInput.getSelectedWiki().orElse(null),
                        userInput.getUser(),
                        passwordInput.getPassword(),
                        this::displayMainWindow,
                        e -> swingCoreServices.errorDialog().showErrorMessage(this, e)))
            .build();
    buttons.add(loginButton);
    final JButton demoButton =
        swingCoreServices
            .component()
            .buttons()
            .builder("Demo", true)
            .withAction(
                component ->
                    demoAction.execute(
                        component,
                        wikiInput.getSelectedWiki().orElse(null),
                        userInput.getUser(),
                        this::displayMainWindow,
                        e -> swingCoreServices.errorDialog().showErrorMessage(this, e)))
            .build();
    buttons.add(demoButton);
    swingCoreServices.layout().addRowSpanningAllColumns(panel, constraints, buttons);

    swingCoreServices
        .layout()
        .addRowSpanningAllColumns(
            panel, constraints, swingCoreServices.component().toolBars().feedbacks());
  }

  private void displayMainWindow() {
    mainWindowFactory.displayMainWindow();
    this.dispose();
  }
}
