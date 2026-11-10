package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.regex.Pattern;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

final class ConfigurationYamlResolver extends Resolver {

  private static final Pattern BOOLEAN = Pattern.compile("^(?:true|True|TRUE|false|False|FALSE)$");

  @Override
  protected void addImplicitResolvers() {
    addImplicitResolver(Tag.BOOL, BOOLEAN, "tTfF");
    addImplicitResolver(Tag.NULL, NULL, "~nN\0");
    addImplicitResolver(Tag.NULL, EMPTY, null);
  }
}
