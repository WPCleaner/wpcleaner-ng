package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.utils.JsonUtils;

class ContentAppendConfigurationTest {

  @DisplayName("ContentAppendConfiguration initializes fields and defaults null to empty")
  @Test
  void testInitializationAndDefaults() {
    final ContentAppendConfiguration config =
        new ContentAppendConfiguration(
            "mySummary", List.of(new ContentAppendConfigurationText(true, "lbl", "txt")));
    Assertions.assertThat(config.summary()).isEqualTo("mySummary");
    Assertions.assertThat(config.texts()).hasSize(1);
    Assertions.assertThat(config.isComplete()).isTrue();

    final ContentAppendConfiguration nullConfig = new ContentAppendConfiguration(null, null);
    Assertions.assertThat(nullConfig.summary()).isEmpty();
    Assertions.assertThat(nullConfig.texts()).isEmpty();
    Assertions.assertThat(nullConfig.isComplete()).isFalse();
  }

  @DisplayName("isComplete returns false when summary is blank or texts are empty/incomplete")
  @Test
  void testIsComplete() {
    final ContentAppendConfigurationText validText =
        new ContentAppendConfigurationText(true, "lbl", "txt");
    final ContentAppendConfigurationText invalidText =
        new ContentAppendConfigurationText(true, "", "txt");

    Assertions.assertThat(new ContentAppendConfiguration("", List.of(validText)).isComplete())
        .isFalse();
    Assertions.assertThat(new ContentAppendConfiguration("summary", List.of()).isComplete())
        .isFalse();
    Assertions.assertThat(
            new ContentAppendConfiguration("summary", List.of(invalidText)).isComplete())
        .isFalse();
    Assertions.assertThat(
            new ContentAppendConfiguration("summary", List.of(validText)).isComplete())
        .isTrue();
  }

  @DisplayName("ContentAppendConfiguration deserializes properly via JSON")
  @Test
  void testJsonDeserialization() {
    final String json =
        """
        {
          "summary": "talkSummary",
          "texts": [
            {
              "addedByDefault": true,
              "label": "lbl1",
              "text": "txt1"
            }
          ]
        }
        """;
    final ContentAppendConfiguration deserialized =
        JsonUtils.readValue(json, ContentAppendConfiguration.class);

    Assertions.assertThat(deserialized.summary()).isEqualTo("talkSummary");
    Assertions.assertThat(deserialized.texts()).hasSize(1);
    Assertions.assertThat(deserialized.texts().getFirst().label()).isEqualTo("lbl1");
    Assertions.assertThat(deserialized.texts().getFirst().text()).isEqualTo("txt1");
  }
}
