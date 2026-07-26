package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.utils.JsonUtils;
import org.wpcleaner.application.gui.settings.recentchanges.RecentChangesSettings;

class RecentChangesSettingsTest {

  @DisplayName("RecentChangesSettings serialization and deserialization works correctly")
  @Test
  void testRecentChangesSettingsSerialization(@TempDir final Path tempDir) throws IOException {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Filter1",
            Set.of(0, 1),
            Severity.ALERT_4,
            Set.of("tag1"),
            Set.of(RecentChangesParameters.Type.EDIT),
            RecentChangesFilter.SubPages.SUB_PAGES);

    final RecentChangesOptions options =
        new RecentChangesOptions(
            "Options1",
            Set.of(2),
            Set.of(RecentChangesParameters.Show.NOT_BOT),
            "tag1",
            Set.of(RecentChangesParameters.Type.NEW),
            true,
            List.of(filter));

    final RecentChangesSettings settings =
        new RecentChangesSettings(1, List.of(options), "Options1");

    final File file = tempDir.resolve("recentchanges.json").toFile();
    JsonUtils.writeValue(file, settings);

    final String jsonContent = Files.readString(file.toPath());
    Assertions.assertThat(jsonContent).contains("Options1");
    Assertions.assertThat(jsonContent).contains("Filter1");

    final RecentChangesSettings loadedSettings =
        JsonUtils.readValue(jsonContent, RecentChangesSettings.class);

    Assertions.assertThat(loadedSettings.version()).isEqualTo(1);
    Assertions.assertThat(loadedSettings.selectedOption()).isEqualTo("Options1");
    Assertions.assertThat(loadedSettings.options()).hasSize(1);

    final RecentChangesOptions loadedOptions = loadedSettings.options().getFirst();
    Assertions.assertThat(loadedOptions.name()).isEqualTo("Options1");
    Assertions.assertThat(loadedOptions.namespace()).containsExactly(2);
    Assertions.assertThat(loadedOptions.show())
        .containsExactly(RecentChangesParameters.Show.NOT_BOT);
    Assertions.assertThat(loadedOptions.tag()).isEqualTo("tag1");
    Assertions.assertThat(loadedOptions.type()).containsExactly(RecentChangesParameters.Type.NEW);
    Assertions.assertThat(loadedOptions.topOnly()).isTrue();
    Assertions.assertThat(loadedOptions.filters()).hasSize(1);

    final RecentChangesFilter loadedFilter = loadedOptions.filters().getFirst();
    Assertions.assertThat(loadedFilter.name()).isEqualTo("Filter1");
    Assertions.assertThat(loadedFilter.namespace()).containsExactlyInAnyOrder(0, 1);
    Assertions.assertThat(loadedFilter.severity()).isEqualTo(Severity.ALERT_4);
    Assertions.assertThat(loadedFilter.tag()).containsExactly("tag1");
    Assertions.assertThat(loadedFilter.type()).containsExactly(RecentChangesParameters.Type.EDIT);
    Assertions.assertThat(loadedFilter.subPages())
        .isEqualTo(RecentChangesFilter.SubPages.SUB_PAGES);
  }
}
