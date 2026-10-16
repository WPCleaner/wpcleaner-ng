package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.utils.JsonUtils;

class ContentAppendConfigurationTextTest {

  @DisplayName(
      "ContentAppendConfigurationText initializes fields and defaults null to false/empty string")
  @Test
  void testInitializationAndDefaults() {
    final ContentAppendConfigurationText config =
        new ContentAppendConfigurationText(true, "myLabel", "myText");
    Assertions.assertThat(config.addedByDefault()).isTrue();
    Assertions.assertThat(config.label()).isEqualTo("myLabel");
    Assertions.assertThat(config.text()).isEqualTo("myText");
    Assertions.assertThat(config.isComplete()).isTrue();

    final ContentAppendConfigurationText nullConfig =
        new ContentAppendConfigurationText(null, null, null);
    Assertions.assertThat(nullConfig.addedByDefault()).isFalse();
    Assertions.assertThat(nullConfig.label()).isEmpty();
    Assertions.assertThat(nullConfig.text()).isEmpty();
    Assertions.assertThat(nullConfig.isComplete()).isFalse();
  }

  @DisplayName("isComplete returns false when label or text is blank")
  @Test
  void testIsComplete() {
    Assertions.assertThat(new ContentAppendConfigurationText(true, "", "text").isComplete())
        .isFalse();
    Assertions.assertThat(new ContentAppendConfigurationText(true, "   ", "text").isComplete())
        .isFalse();
    Assertions.assertThat(new ContentAppendConfigurationText(true, "label", "").isComplete())
        .isFalse();
    Assertions.assertThat(new ContentAppendConfigurationText(true, "label", "   ").isComplete())
        .isFalse();
    Assertions.assertThat(new ContentAppendConfigurationText(true, "label", "text").isComplete())
        .isTrue();
  }

  @DisplayName("ContentAppendConfigurationText deserializes properly via JSON")
  @Test
  void testJsonDeserialization() {
    final String json =
        """
        {
          "addedByDefault": true,
          "label": "testLabel",
          "text": "testText"
        }
        """;
    final ContentAppendConfigurationText deserialized =
        JsonUtils.readValue(json, ContentAppendConfigurationText.class);

    Assertions.assertThat(deserialized.addedByDefault()).isTrue();
    Assertions.assertThat(deserialized.label()).isEqualTo("testLabel");
    Assertions.assertThat(deserialized.text()).isEqualTo("testText");
  }
}
