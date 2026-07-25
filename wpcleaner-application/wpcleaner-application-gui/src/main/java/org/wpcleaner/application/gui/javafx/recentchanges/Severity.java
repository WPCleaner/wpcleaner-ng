package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.image.ImageCollection;

public enum Severity {
  NOTICE_0(0, GT._T("Notice, green"), ImageCollection.NOTICE_SUCCESS),
  NOTICE_1(1, GT._T("Notice, black"), ImageCollection.NOTICE),
  NOTICE_2(2, GT._T("Notice, grey"), ImageCollection.NOTICE_PLACEHOLDER),
  NOTICE_3(3, GT._T("Notice, blue"), ImageCollection.NOTICE_PROGRESSIVE),
  NOTICE_4(4, GT._T("Notice, gold"), ImageCollection.NOTICE_WARNING),
  NOTICE_5(5, GT._T("Notice, red"), ImageCollection.ERROR),
  ALERT_0(10, GT._T("Alert, green"), ImageCollection.ALERT_SUCCESS),
  ALERT_1(11, GT._T("Alert, black"), ImageCollection.ALERT),
  ALERT_2(12, GT._T("Alert, grey"), ImageCollection.ALERT_PLACEHOLDER),
  ALERT_3(13, GT._T("Alert, blue"), ImageCollection.ALERT_PROGRESSIVE),
  ALERT_4(14, GT._T("Alert, gold"), ImageCollection.ALERT_WARNING),
  ALERT_5(15, GT._T("Alert, yellow"), ImageCollection.ALERT_YELLOW),
  ALERT_6(16, GT._T("Alert, orange"), ImageCollection.ALERT_ORANGE),
  ALERT_7(17, GT._T("Alert, red"), ImageCollection.ALERT_ERROR),
  ERROR_0(20, GT._T("Error, green"), ImageCollection.ERROR_SUCCESS),
  ERROR_1(21, GT._T("Error, black"), ImageCollection.ERROR),
  ERROR_2(22, GT._T("Error, grey"), ImageCollection.ERROR_PLACEHOLDER),
  ERROR_3(23, GT._T("Error, blue"), ImageCollection.ERROR_PROGRESSIVE),
  ERROR_4(24, GT._T("Error, gold"), ImageCollection.ERROR_WARNING),
  ERROR_5(25, GT._T("Error, red"), ImageCollection.ERROR_ERROR);

  private final int id;
  private final String name;
  private final ImageCollection image;

  Severity(final int id, final String name, final ImageCollection image) {
    this.id = id;
    this.name = name;
    this.image = image;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ImageCollection getImage() {
    return image;
  }
}
