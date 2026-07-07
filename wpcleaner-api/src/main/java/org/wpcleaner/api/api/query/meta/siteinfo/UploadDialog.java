package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public record UploadDialog(
    @JsonProperty("fields") @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, Object> fields,
    @JsonProperty("licensemessages") @Nullable LicenseMessages licenseMessages) {}
