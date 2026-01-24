package org.wpcleaner.application.gui.swing.core.worker;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class ProgressPanel extends JComponent {

  @Serial private static final long serialVersionUID = 0;
  private static final String TITLE = "WPCleaner is working";

  private final transient Map<Object, Object> hints;
  private final Color background;
  private final Color textBackground;

  @Nullable private transient Component recentFocusOwner;

  public ProgressPanel() {
    hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    final int alphaLevel = 255;
    final float shield = 0.7F;
    background = new Color(255, 255, 255, (int) (alphaLevel * shield));
    textBackground = new Color(0, 255, 255, (int) (alphaLevel * shield));
  }

  @Override
  public void setVisible(final boolean visible) {
    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    final boolean oldVisible = isVisible();
    super.setVisible(visible);
    final JRootPane rootPane = getRootPane();
    if (rootPane == null || isVisible() == oldVisible) {
      return;
    }
    if (isVisible()) {
      final Component focusOwner =
          KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
      if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, rootPane)) {
        recentFocusOwner = focusOwner;
      }
      rootPane.getLayeredPane().setVisible(false);
      requestFocusInWindow();
    } else {
      rootPane.getLayeredPane().setVisible(true);
      if (recentFocusOwner != null) {
        recentFocusOwner.requestFocusInWindow();
      }
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    drawLayeredPane(g);
    drawBackground(g);
    drawText(g);
  }

  private void drawLayeredPane(final Graphics g) {
    final JRootPane rootPane = getRootPane();
    if (rootPane != null) {
      // It is important to call print() instead of paint() here
      // because print() doesn't affect the frame's double buffer
      rootPane.getLayeredPane().print(g);
    }
  }

  private void drawBackground(final Graphics g) {
    final int width = getWidth();
    final int height = getHeight();
    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHints(hints);
    g2d.setColor(background);
    g2d.fillRect(0, 0, width, height);
  }

  private void drawText(final Graphics g) {
    // Positions
    float xMin = Float.MAX_VALUE;
    float xMax = Float.MIN_VALUE;
    float yMin = Float.MAX_VALUE;
    float yMax = Float.MIN_VALUE;
    final float xSpace = 10F;
    final float ySpace = 5F;

    // Compute bonds
    final Graphics2D g2d = (Graphics2D) g;
    final int width = getWidth();
    final int height = getHeight();
    final FontRenderContext context = g2d.getFontRenderContext();
    final TextLayout layoutApplication = new TextLayout(TITLE, getFont(), context);
    final Rectangle2D boundsApplication = layoutApplication.getBounds();
    final float xApplication = (float) (width - boundsApplication.getWidth()) / 2;
    final float yApplication =
        Math.max((float) ((float) height / 2 - 2 * boundsApplication.getHeight()), 0);
    xMin = Math.min(xMin, (float) (xApplication + boundsApplication.getMinX() - xSpace));
    xMax = Math.max(xMax, (float) (xApplication + boundsApplication.getMaxX() + xSpace));
    yMin = Math.min(yMin, (float) (yApplication + boundsApplication.getMinY() - ySpace));
    yMax = Math.max(yMax, (float) (yApplication + boundsApplication.getMaxY() + ySpace));

    // Draw border
    if ((xMin < xMax) && (yMin < yMax)) {
      g2d.setColor(textBackground);
      g2d.fillRoundRect((int) xMin, (int) yMin, (int) (xMax - xMin), (int) (yMax - yMin), 10, 10);
      g2d.setColor(Color.BLUE);
      g2d.drawRoundRect((int) xMin, (int) yMin, (int) (xMax - xMin), (int) (yMax - yMin), 10, 10);
    }

    // Draw text
    g2d.setColor(getForeground());
    layoutApplication.draw(g2d, xApplication, yApplication);
  }
}
