package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.utils.JsonUtils;

class ContentReplaceConfigurationTest {

  @DisplayName("ContentReplaceConfiguration initializes fields and defaults null to empty string")
  @Test
  void testInitializationAndDefaults() {
    final ContentReplaceConfiguration config =
        new ContentReplaceConfiguration("mySummary", "myText");
    Assertions.assertThat(config.summary()).isEqualTo("mySummary");
    Assertions.assertThat(config.text()).isEqualTo("myText");
    Assertions.assertThat(config.isComplete()).isTrue();

    final ContentReplaceConfiguration nullConfig = new ContentReplaceConfiguration(null, null);
    Assertions.assertThat(nullConfig.summary()).isEmpty();
    Assertions.assertThat(nullConfig.text()).isEmpty();
    Assertions.assertThat(nullConfig.isComplete()).isFalse();
  }

  @DisplayName("isComplete returns false when summary or text is blank")
  @Test
  void testIsComplete() {
    Assertions.assertThat(new ContentReplaceConfiguration("", "text").isComplete()).isFalse();
    Assertions.assertThat(new ContentReplaceConfiguration("   ", "text").isComplete()).isFalse();
    Assertions.assertThat(new ContentReplaceConfiguration("summary", "").isComplete()).isFalse();
    Assertions.assertThat(new ContentReplaceConfiguration("summary", "   ").isComplete()).isFalse();
    Assertions.assertThat(new ContentReplaceConfiguration("summary", "text").isComplete()).isTrue();
  }

  @DisplayName("ContentReplaceConfiguration deserializes properly via JSON")
  @Test
  void testJsonDeserialization() {
    final String json =
        """
        {
          "summary": "testSummary",
          "text": "testText"
        }
        """;
    final ContentReplaceConfiguration deserialized =
        JsonUtils.readValue(json, ContentReplaceConfiguration.class);

    Assertions.assertThat(deserialized.summary()).isEqualTo("testSummary");
    Assertions.assertThat(deserialized.text()).isEqualTo("testText");
  }
}
