package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackageClasses = CheckAlgorithm.class)
@EnableAutoConfiguration
public class CheckSpringBootTestConfiguration {}
