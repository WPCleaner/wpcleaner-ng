package org.wpcleaner.gui.swing.core.configuration;

import jakarta.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to detect Event Dispatch Thread rule violations.
 *
 * <p>This is a modified version of code by Scott Delap and Alexander Potochkin.<br>
 * A version of this code can be found at <a
 * href="http://www.java2s.com/Code/Java/Swing-JFC/DetectEventDispatchThreadruleviolations.htm">Detect
 * Event Dispatch Thread rule violations</a>
 */
public class CheckThreadViolationRepaintManager extends RepaintManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String IMAGE_UPDATE_METHOD = "imageUpdate";
  private static final String REPAINT_METHOD = "repaint";

  private final boolean completeCheck;
  @Nullable private WeakReference<JComponent> lastComponent;

  CheckThreadViolationRepaintManager(final boolean completeCheck) {
    this.completeCheck = completeCheck;
  }

  @Override
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  public synchronized void addInvalidComponent(final JComponent invalidComponent) {
    checkThreadViolations(invalidComponent);
    super.addInvalidComponent(invalidComponent);
  }

  @Override
  public void addDirtyRegion(
      final JComponent c, final int x, final int y, final int w, final int h) {
    checkThreadViolations(c);
    super.addDirtyRegion(c, x, y, w, h);
  }

  private void checkThreadViolations(final JComponent c) {
    if (SwingUtilities.isEventDispatchThread()) {
      return;
    }
    if (!completeCheck && !c.isShowing()) {
      return;
    }

    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (checkStackTrace(stackTrace)) {
      return;
    }

    // Ignore the last processed component
    if (lastComponent != null && Objects.equals(c, lastComponent.get())) {
      return;
    }

    reportViolation(stackTrace, c);
  }

  private boolean checkStackTrace(final StackTraceElement... stackTrace) {
    boolean repaint = false;
    boolean fromSwing = false;
    for (final StackTraceElement ste : stackTrace) {
      if (repaint && ste.getClassName().startsWith("javax.swing")) {
        fromSwing = true;
      }
      if (repaint && IMAGE_UPDATE_METHOD.equals(ste.getMethodName())) {
        // Assuming it's java.awt.image.ImageObserver.imageUpdate(...)
        // Image was asynchronously updated, that's ok
        return true;
      }
      if (REPAINT_METHOD.equals(ste.getMethodName())) {
        repaint = true;
        fromSwing = false;
      }
    }

    // repaint() is thread safe
    return repaint && !fromSwing;
  }

  private void reportViolation(final StackTraceElement[] stackTrace, final JComponent c) {
    lastComponent = new WeakReference<>(c);
    final StringBuilder builder = new StringBuilder("EDT violation detected on ").append(c);
    Arrays.stream(stackTrace).forEachOrdered(ste -> builder.append("\n\tat ").append(ste));
    LOGGER.error(builder.toString());
  }
}
