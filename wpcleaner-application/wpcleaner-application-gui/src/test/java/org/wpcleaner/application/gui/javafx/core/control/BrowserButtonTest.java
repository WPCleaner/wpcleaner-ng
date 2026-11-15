package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;

@GuiSpringBootTest
class BrowserButtonTest extends JavaFxTest {

  @Autowired private ImageLoader imageLoader;

  @DisplayName("should browse the current URI, and nothing when there is none")
  @Test
  void browse() throws InterruptedException, ExecutionException, TimeoutException {
    // GIVEN
    final List<String> browsed = new ArrayList<>();
    final List<@Nullable URI> uris =
        new ArrayList<>(List.of(URI.create("https://fr.wikipedia.org/wiki/Paris")));
    final BrowserButton button =
        callOnJavaFx(
            () ->
                new BrowserButton(
                    new JavaFxImageLoader(imageLoader),
                    ImageCollection.OPEN_URL,
                    "Open",
                    uris::getFirst,
                    browsed::add));

    // WHEN
    runOnJavaFx(button::fire);
    uris.set(0, null);
    runOnJavaFx(button::fire);

    // THEN
    Assertions.assertThat(browsed).containsExactly("https://fr.wikipedia.org/wiki/Paris");
    Assertions.assertThat(button.getTooltip().getText()).isEqualTo("Open");
    Assertions.assertThat(button.getStyle()).isEqualTo(DefaultStyles.TOOLBAR_ELEMENT);
  }
}
