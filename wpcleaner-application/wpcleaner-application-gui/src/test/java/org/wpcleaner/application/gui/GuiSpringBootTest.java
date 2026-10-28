package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wpcleaner.api.api.MediaWikiServerConfiguration;
import org.wpcleaner.application.gui.core.desktop.DesktopService;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(
    classes = {WPCleaner.class, MediaWikiServerConfiguration.class, GuiLogin.class},
    properties = "wpcleaner.api.edit-wrapper.throttle-period-seconds=0",
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
@MockitoBean(types = DesktopService.class)
public @interface GuiSpringBootTest {}
