package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.image.ImageCollection;

public enum Severity {
  @JsonProperty("notice_0")
  NOTICE_0(GT._T("Notice, green"), ImageCollection.NOTICE_SUCCESS),
  @JsonProperty("notice_1")
  NOTICE_1(GT._T("Notice, black"), ImageCollection.NOTICE),
  @JsonProperty("notice_2")
  NOTICE_2(GT._T("Notice, grey"), ImageCollection.NOTICE_PLACEHOLDER),
  @JsonProperty("notice_3")
  NOTICE_3(GT._T("Notice, blue"), ImageCollection.NOTICE_PROGRESSIVE),
  @JsonProperty("notice_4")
  NOTICE_4(GT._T("Notice, gold"), ImageCollection.NOTICE_WARNING),
  @JsonProperty("notice_5")
  NOTICE_5(GT._T("Notice, red"), ImageCollection.ERROR),
  @JsonProperty("alert_0")
  ALERT_0(GT._T("Alert, green"), ImageCollection.ALERT_SUCCESS),
  @JsonProperty("alert_1")
  ALERT_1(GT._T("Alert, black"), ImageCollection.ALERT),
  @JsonProperty("alert_2")
  ALERT_2(GT._T("Alert, grey"), ImageCollection.ALERT_PLACEHOLDER),
  @JsonProperty("alert_3")
  ALERT_3(GT._T("Alert, blue"), ImageCollection.ALERT_PROGRESSIVE),
  @JsonProperty("alert_4")
  ALERT_4(GT._T("Alert, gold"), ImageCollection.ALERT_WARNING),
  @JsonProperty("alert_5")
  ALERT_5(GT._T("Alert, yellow"), ImageCollection.ALERT_YELLOW),
  @JsonProperty("alert_6")
  ALERT_6(GT._T("Alert, orange"), ImageCollection.ALERT_ORANGE),
  @JsonProperty("alert_7")
  ALERT_7(GT._T("Alert, red"), ImageCollection.ALERT_ERROR),
  @JsonProperty("error_0")
  ERROR_0(GT._T("Error, green"), ImageCollection.ERROR_SUCCESS),
  @JsonProperty("error_1")
  ERROR_1(GT._T("Error, black"), ImageCollection.ERROR),
  @JsonProperty("error_2")
  ERROR_2(GT._T("Error, grey"), ImageCollection.ERROR_PLACEHOLDER),
  @JsonProperty("error_3")
  ERROR_3(GT._T("Error, blue"), ImageCollection.ERROR_PROGRESSIVE),
  @JsonProperty("error_4")
  ERROR_4(GT._T("Error, gold"), ImageCollection.ERROR_WARNING),
  @JsonProperty("error_5")
  ERROR_5(GT._T("Error, red"), ImageCollection.ERROR_ERROR);

  private final String name;
  private final ImageCollection image;

  Severity(final String name, final ImageCollection image) {
    this.name = name;
    this.image = image;
  }

  public String getName() {
    return name;
  }

  public ImageCollection getImage() {
    return image;
  }
}
