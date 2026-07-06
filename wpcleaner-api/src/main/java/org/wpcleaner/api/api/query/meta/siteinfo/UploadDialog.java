package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.Map;

public record UploadDialog(
    @JsonProperty("fields") @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, Object> fields,
    @JsonProperty("licensemessages") @Nullable LicenseMessages licenseMessages) {}
