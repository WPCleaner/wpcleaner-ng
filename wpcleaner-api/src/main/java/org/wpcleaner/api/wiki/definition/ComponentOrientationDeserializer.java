package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.awt.ComponentOrientation;
import java.io.IOException;

final class ComponentOrientationDeserializer extends JsonDeserializer<ComponentOrientation> {

  @Override
  public ComponentOrientation deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    return p.getValueAsBoolean()
        ? ComponentOrientation.LEFT_TO_RIGHT
        : ComponentOrientation.RIGHT_TO_LEFT;
  }
}
