package org.wpcleaner.application.bot;

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
import org.wpcleaner.application.base.ApplicationLogin;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(
    classes = {
      WPCleanerBot.class,
      MediaWikiServerConfiguration.class,
      ApplicationLogin.class,
      BotCredentialsReader.class
    },
    properties = {
      MediaWikiServerConfiguration.ENABLED_PROPERTY + "=true",
      "wpcleaner.bot.requests-per-minute.user=60000",
      "wpcleaner.bot.requests-per-minute.bot=60000"
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
public @interface BotSpringBootTest {}
