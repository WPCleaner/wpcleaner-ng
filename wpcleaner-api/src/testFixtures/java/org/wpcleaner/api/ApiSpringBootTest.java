package org.wpcleaner.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.api.MediaWikiServerConfiguration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(
    classes = {ApiSpringBootTestConfiguration.class, MediaWikiServerConfiguration.class},
    properties = MediaWikiServerConfiguration.ENABLED_PROPERTY + "=true")
public @interface ApiSpringBootTest {}
