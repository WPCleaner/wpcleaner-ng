package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

@GuiSpringBootTest
class RecentChangesDifferencesPanelTest extends JavaFxTest {

  private static final String RIGHT_TO_LEFT_TEXT =
      "= الأمين الحاجي =\n\n"
          + "'''الدكتور الأمين الحاجي''' (مواليد 1986 في المتلوي، تونس) هو طبيب عام في الصحة"
          + " العمومية وطبيب استعجالي بالمستشفى الجهوي بالمتلوي.";
  private static final double WINDOW_SIZE = 300;

  @Autowired private JavaFxStylePropertiesRegistry styleRegistry;

  @DisplayName("should display the first revision of a right-to-left page")
  @Test
  void displayRightToLeftFirstRevision()
      throws ExecutionException, InterruptedException, TimeoutException {
    // GIVEN
    final RecentChangesDifferencesPanel panel =
        callOnJavaFx(() -> new RecentChangesDifferencesPanel(styleRegistry));

    // WHEN
    final Throwable thrown =
        Assertions.catchThrowable(
            () ->
                runOnJavaFx(
                    () -> {
                      final Stage stage = new Stage();
                      stage.setScene(new Scene(panel, WINDOW_SIZE, WINDOW_SIZE));
                      stage.show();
                      panel.updateContents(RIGHT_TO_LEFT_TEXT, null, List.of());
                      stage.getScene().getRoot().layout();
                      stage.close();
                    }));

    // THEN
    Assertions.assertThat(thrown).isNull();
  }
}
