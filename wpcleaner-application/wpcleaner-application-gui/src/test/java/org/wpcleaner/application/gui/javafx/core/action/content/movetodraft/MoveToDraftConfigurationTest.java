package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.utils.JsonUtils;

class MoveToDraftConfigurationTest {

  @DisplayName("MoveToDraftConfiguration initializes fields and defaults null to empty string")
  @Test
  void testInitializationAndDefaults() {
    final MoveToDraftConfiguration config =
        new MoveToDraftConfiguration("Draft", "Moving to draft");
    Assertions.assertThat(config.subPageName()).isEqualTo("Draft");
    Assertions.assertThat(config.summary()).isEqualTo("Moving to draft");
    Assertions.assertThat(config.isComplete()).isTrue();

    final MoveToDraftConfiguration nullConfig = new MoveToDraftConfiguration(null, null);
    Assertions.assertThat(nullConfig.subPageName()).isEmpty();
    Assertions.assertThat(nullConfig.summary()).isEmpty();
    Assertions.assertThat(nullConfig.isComplete()).isFalse();
  }

  @DisplayName("isComplete returns false when subPageName or summary is blank")
  @Test
  void testIsComplete() {
    Assertions.assertThat(new MoveToDraftConfiguration("", "summary").isComplete()).isFalse();
    Assertions.assertThat(new MoveToDraftConfiguration("   ", "summary").isComplete()).isFalse();
    Assertions.assertThat(new MoveToDraftConfiguration("Draft", "").isComplete()).isFalse();
    Assertions.assertThat(new MoveToDraftConfiguration("Draft", "   ").isComplete()).isFalse();
    Assertions.assertThat(new MoveToDraftConfiguration("Draft", "summary").isComplete()).isTrue();
  }

  @DisplayName("MoveToDraftConfiguration deserializes properly via JSON")
  @Test
  void testJsonDeserialization() {
    final String json =
        """
        {
          "subPageName": "DraftSubPage",
          "summary": "Moving page to draft"
        }
        """;
    final MoveToDraftConfiguration deserialized =
        JsonUtils.readValue(json, MoveToDraftConfiguration.class);

    Assertions.assertThat(deserialized.subPageName()).isEqualTo("DraftSubPage");
    Assertions.assertThat(deserialized.summary()).isEqualTo("Moving page to draft");
  }
}
