package org.wpcleaner.launcher.display;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.jspecify.annotations.Nullable;

public final class SwingDisplay implements LauncherDisplay {

  private static final String TITLE = "WPCleaner NG";
  private static final String LOGO = "/org/wpcleaner/launcher/logo.png";
  private static final int PROGRESS_MAXIMUM = 1000;
  private static final int MARGIN = 12;
  private static final int LOGO_SIZE = 64;
  private static final int STATUS_WIDTH = 360;

  private Optional<ProgressWindow> progressWindow = Optional.empty();

  private record ProgressWindow(JFrame frame, JLabel status, JProgressBar progressBar) {}

  public SwingDisplay() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (final ReflectiveOperationException | UnsupportedLookAndFeelException e) {
      throw new IllegalStateException("System look and feel is not available", e);
    }
  }

  @Override
  public void progress(final String status, final long current, final long total) {
    SwingUtilities.invokeLater(
        () -> {
          final ProgressWindow window = openProgressWindow();
          window.status().setText(status);
          window.progressBar().setIndeterminate(total <= 0);
          if (total > 0) {
            window.progressBar().setValue((int) (current * PROGRESS_MAXIMUM / total));
          }
        });
  }

  @Override
  public void closeProgress() {
    SwingUtilities.invokeLater(
        () -> {
          progressWindow.ifPresent(window -> window.frame().dispose());
          progressWindow = Optional.empty();
        });
  }

  @Override
  public void inform(final String message) {
    runAndWait(() -> showMessage(message, JOptionPane.INFORMATION_MESSAGE));
  }

  @Override
  public void error(final String message) {
    runAndWait(() -> showMessage(message, JOptionPane.ERROR_MESSAGE));
  }

  @Override
  public boolean confirm(final String message) {
    final AtomicBoolean confirmed = new AtomicBoolean();
    runAndWait(
        () ->
            confirmed.set(
                JOptionPane.showConfirmDialog(
                        dialogParent(),
                        message,
                        TITLE,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION));
    return confirmed.get();
  }

  private void showMessage(final String message, final int messageType) {
    JOptionPane.showMessageDialog(dialogParent(), message, TITLE, messageType);
  }

  private @Nullable JFrame dialogParent() {
    return progressWindow.map(ProgressWindow::frame).orElse(null);
  }

  private ProgressWindow openProgressWindow() {
    if (progressWindow.isPresent()) {
      return progressWindow.get();
    }
    final JFrame frame = new JFrame(TITLE);
    final JLabel status = new JLabel(TITLE);
    final JProgressBar progressBar = new JProgressBar(0, PROGRESS_MAXIMUM);
    final JPanel content = new JPanel(new BorderLayout(MARGIN, MARGIN));
    content.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
    content.add(status, BorderLayout.CENTER);
    content.add(progressBar, BorderLayout.SOUTH);
    status.setPreferredSize(new Dimension(STATUS_WIDTH, LOGO_SIZE));
    final URL logo = SwingDisplay.class.getResource(LOGO);
    if (logo != null) {
      final ImageIcon icon = new ImageIcon(logo);
      frame.setIconImage(icon.getImage());
      content.add(
          new JLabel(
              new ImageIcon(
                  icon.getImage().getScaledInstance(LOGO_SIZE, LOGO_SIZE, Image.SCALE_SMOOTH))),
          BorderLayout.WEST);
    }
    frame.setContentPane(content);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    final ProgressWindow window = new ProgressWindow(frame, status, progressBar);
    progressWindow = Optional.of(window);
    return window;
  }

  private static void runAndWait(final Runnable action) {
    try {
      SwingUtilities.invokeAndWait(action);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (final InvocationTargetException e) {
      throw new IllegalStateException("Display failure", e);
    }
  }
}
