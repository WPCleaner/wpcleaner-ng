package org.wpcleaner.application.gui.swing.core.dialog;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.application.gui.core.desktop.DesktopService;

@Service
public class ErrorDialogService {

  private static final String TITLE = "WPCleaner - Error";

  private final DesktopService desktopService;

  public ErrorDialogService(final DesktopService desktopService) {
    this.desktopService = Objects.requireNonNull(desktopService);
  }

  public void showErrorMessage(final Component parent, final Throwable e) {
    Throwable current = e;
    if (current instanceof ExecutionException executionException) {
      current = Objects.requireNonNullElse(executionException.getCause(), executionException);
    }
    if (!(current instanceof ApiException apiException)) {
      JOptionPane.showMessageDialog(
          parent,
          "%s: %s"
              .formatted(
                  current.getClass().getSimpleName(),
                  Objects.requireNonNullElse(current.getMessage(), "Unknown error")),
          TITLE,
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    final JEditorPane editorPane = createEditorPane(apiException);
    JOptionPane.showMessageDialog(parent, editorPane, TITLE, JOptionPane.ERROR_MESSAGE);
  }

  private JEditorPane createEditorPane(final ApiException apiException) {
    final JLabel label = new JLabel();
    final Font font = label.getFont();
    final Color background = label.getBackground();
    final JEditorPane editorPane =
        new JEditorPane(
            "text/html",
            """
            <html><body style="font-family:%s;font-weight:%s;font-size:%dpt;background-color: rgb(%d, %d, %d);">
            <center><b>%s</b></center><br/>
            <b>Details</b>:<br/>
            %s
            </body></html>
            """
                .formatted(
                    font.getFamily(),
                    font.isBold() ? "bold" : "normal",
                    font.getSize(),
                    background.getRed(),
                    background.getGreen(),
                    background.getBlue(),
                    apiException.getMessage(),
                    apiException.getDetails()));
    editorPane.addHyperlinkListener(this::hyperlinkUpdate);
    editorPane.setBackground(background);
    editorPane.setBorder(BorderFactory.createLineBorder(background));
    editorPane.setEditable(false);
    editorPane.setMargin(new Insets(0, 0, 0, 0));
    // TODO: Find a way to avoid a graphical artifact (white vertical line on the right)
    return editorPane;
  }

  private void hyperlinkUpdate(final HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      desktopService.browse(e.getURL().toString());
    }
  }
}
