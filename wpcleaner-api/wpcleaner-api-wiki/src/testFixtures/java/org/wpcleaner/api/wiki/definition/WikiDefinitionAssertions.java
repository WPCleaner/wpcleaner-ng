package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

public final class WikiDefinitionAssertions
    extends AbstractObjectAssert<WikiDefinitionAssertions, WikiDefinition> {

  private WikiDefinitionAssertions(final WikiDefinition definition) {
    super(definition, WikiDefinitionAssertions.class);
  }

  public static WikiDefinitionAssertions assertThat(final WikiDefinition definition) {
    return new WikiDefinitionAssertions(definition);
  }

  public AbstractCollectionAssert<?, Collection<? extends String>, String, ObjectAssert<String>>
      assertThatHosts() {
    Assertions.assertThat(actual).isNotNull();
    return Assertions.assertThat(actual.hosts());
  }
}
