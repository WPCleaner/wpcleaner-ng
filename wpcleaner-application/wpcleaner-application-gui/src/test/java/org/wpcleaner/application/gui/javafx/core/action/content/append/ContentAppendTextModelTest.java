package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContentAppendTextModelTest {

  @DisplayName("ContentAppendTextModel initializes properties and converts to config")
  @Test
  void testModelPropertiesAndConversion() {
    final ContentAppendTextModel model = new ContentAppendTextModel(true, "testLabel", "testText");
    Assertions.assertThat(model.addedByDefaultProperty().get()).isTrue();
    Assertions.assertThat(model.labelProperty().get()).isEqualTo("testLabel");
    Assertions.assertThat(model.textProperty().get()).isEqualTo("testText");

    final ContentAppendConfigurationText config = model.toConfig();
    Assertions.assertThat(config.addedByDefault()).isTrue();
    Assertions.assertThat(config.label()).isEqualTo("testLabel");
    Assertions.assertThat(config.text()).isEqualTo("testText");

    final ContentAppendTextModel fromConfig = new ContentAppendTextModel(config);
    Assertions.assertThat(fromConfig.addedByDefaultProperty().get()).isTrue();
    Assertions.assertThat(fromConfig.labelProperty().get()).isEqualTo("testLabel");
    Assertions.assertThat(fromConfig.textProperty().get()).isEqualTo("testText");
  }
}
