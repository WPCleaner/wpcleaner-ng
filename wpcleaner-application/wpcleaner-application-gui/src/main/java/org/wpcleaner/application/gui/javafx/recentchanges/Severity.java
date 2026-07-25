package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.lib.image.ImageCollection;

public enum Severity {
  NOTICE_0(0, "Notice, green", ImageCollection.NOTICE_SUCCESS),
  NOTICE_1(1, "Notice, black", ImageCollection.NOTICE),
  NOTICE_2(2, "Notice, grey", ImageCollection.NOTICE_PLACEHOLDER),
  NOTICE_3(3, "Notice, blue", ImageCollection.NOTICE_PROGRESSIVE),
  NOTICE_4(4, "Notice, gold", ImageCollection.NOTICE_WARNING),
  NOTICE_5(5, "Notice, red", ImageCollection.ERROR),
  ALERT_0(10, "Alert, green", ImageCollection.ALERT_SUCCESS),
  ALERT_1(11, "Alert, black", ImageCollection.ALERT),
  ALERT_2(12, "Alert, grey", ImageCollection.ALERT_PLACEHOLDER),
  ALERT_3(13, "Alert, blue", ImageCollection.ALERT_PROGRESSIVE),
  ALERT_4(14, "Alert, gold", ImageCollection.ALERT_WARNING),
  ALERT_5(15, "Alert, yellow", ImageCollection.ALERT_YELLOW),
  ALERT_6(16, "Alert, orange", ImageCollection.ALERT_ORANGE),
  ALERT_7(17, "Alert, red", ImageCollection.ALERT_ERROR),
  ERROR_0(20, "Error, green", ImageCollection.ERROR_SUCCESS),
  ERROR_1(21, "Error, black", ImageCollection.ERROR),
  ERROR_2(22, "Error, grey", ImageCollection.ERROR_PLACEHOLDER),
  ERROR_3(23, "Error, blue", ImageCollection.ERROR_PROGRESSIVE),
  ERROR_4(24, "Error, gold", ImageCollection.ERROR_WARNING),
  ERROR_5(25, "Error, red", ImageCollection.ERROR_ERROR);

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
