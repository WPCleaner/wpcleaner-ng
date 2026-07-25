package org.wpcleaner.application.gui.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "org.wpcleaner.application.gui.javafx")
@ConditionalOnProperty(name = "gui", havingValue = "javafx")
@Configuration
public class JavaFxConfiguration {}
