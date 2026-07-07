package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface ApiResponse {

  List<ApiError> errors();

  List<ApiError> warnings();

  @Nullable String docref();
}
