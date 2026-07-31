package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.awt.ComponentOrientation;
import java.io.IOException;

final class ComponentOrientationSerializer extends JsonSerializer<ComponentOrientation> {

  @Override
  public void serialize(
      final ComponentOrientation value,
      final JsonGenerator gen,
      final SerializerProvider serializers)
      throws IOException {
    gen.writeBoolean(value == ComponentOrientation.LEFT_TO_RIGHT);
  }
}
