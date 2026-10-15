package org.wpcleaner.application.gui.javafx.core.action.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

class PageContentDisplayTest extends JavaFxTest {

  @DisplayName("PageContentDisplay initializes children and properties properly")
  @Test
  void testInitialization() throws InterruptedException, ExecutionException, TimeoutException {
    runOnJavaFx(
        () -> {
          final PageSyntaxColorizer colorizer =
              new PageSyntaxColorizer(
                  List.of(),
                  new JavaFxStylePropertiesRegistry(new StylePropertiesRegistry(List.of())));
          final PageAnalysisFactory pageAnalysisFactory =
              new PageAnalysisFactory(
                  new InterwikiRepository(), new NamespaceRepository(), new ProtocolRepository());

          final PageContentDisplay display =
              new PageContentDisplay(colorizer, pageAnalysisFactory, "User:Test", "Page content");

          Assertions.assertThat(display.getChildren()).hasSize(2);
          final Label label = (Label) display.getChildren().getFirst();
          Assertions.assertThat(label.getText()).isEqualTo("User:Test");

          final PageAnalysisScrollPane scrollPane = display.getScrollPane();
          Assertions.assertThat(scrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(display.getArea()).isNotNull();

          Assertions.assertThat(display.getPrefWidth()).isEqualTo(0.0);
          Assertions.assertThat(display.getMaxWidth()).isEqualTo(Double.MAX_VALUE);
          Assertions.assertThat(HBox.getHgrow(display)).isEqualTo(Priority.ALWAYS);

          display.updateText("User:Updated", "Updated content", pageAnalysisFactory);
          Assertions.assertThat(label.getText()).isEqualTo("User:Updated");
        });
  }
}
