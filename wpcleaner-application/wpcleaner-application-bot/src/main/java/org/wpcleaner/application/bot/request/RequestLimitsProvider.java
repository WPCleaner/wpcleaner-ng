package org.wpcleaner.application.bot.request;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.CurrentUserService;

@Service
public class RequestLimitsProvider {

  public static final String BOT_GROUP = "bot";
  public static final String HIGH_LIMITS_RIGHT = "apihighlimits";
  public static final int MAX_PAGE_IDS = 50;
  public static final int MAX_PAGE_IDS_WITH_HIGH_LIMITS = 500;

  private final CurrentUserService currentUserService;
  private final int userRequestsPerMinute;
  private final int botRequestsPerMinute;

  public RequestLimitsProvider(
      final CurrentUserService currentUserService,
      @Value("${wpcleaner.bot.requests-per-minute.user:3}") final int userRequestsPerMinute,
      @Value("${wpcleaner.bot.requests-per-minute.bot:10}") final int botRequestsPerMinute) {
    this.currentUserService = currentUserService;
    this.userRequestsPerMinute = userRequestsPerMinute;
    this.botRequestsPerMinute = botRequestsPerMinute;
  }

  public RequestLimits getLimits() {
    final ConnectedUser user = currentUserService.getCurrentUser();
    return new RequestLimits(
        user.rights().contains(HIGH_LIMITS_RIGHT) ? MAX_PAGE_IDS_WITH_HIGH_LIMITS : MAX_PAGE_IDS,
        user.groups().contains(BOT_GROUP) ? botRequestsPerMinute : userRequestsPerMinute);
  }
}
