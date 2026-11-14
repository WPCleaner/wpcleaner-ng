package org.wpcleaner.application.bot.script;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    @JsonSubTypes.Type(
        value = ProcessContentCurrentTask.class,
        name = ProcessContentCurrentTask.TYPE))
public sealed interface BotTask permits ProcessContentCurrentTask {

  Set<Integer> algorithms();
}
