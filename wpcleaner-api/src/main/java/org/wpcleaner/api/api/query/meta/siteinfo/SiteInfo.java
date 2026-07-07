package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;

public record SiteInfo(
    @JsonProperty("dbrepllag") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<DbReplicationLag> dbReplicationLag,
    @JsonProperty("defaultoptions") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, Object> defaultOptions,
    @JsonProperty("extensions") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Extension> extensions,
    @JsonProperty("extensiontags") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> extensionTags,
    @JsonProperty("fileextensions") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<FileExtension> fileExtensions,
    @JsonProperty("functionhooks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> functionHooks,
    @JsonProperty("general") @Nullable General general,
    @JsonProperty("interwikimap") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Interwiki> interwikiMap,
    @JsonProperty("languages") @JsonSetter(nulls = Nulls.AS_EMPTY) List<SiteLanguage> languages,
    @JsonProperty("languagevariants") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, Map<String, LanguageVariant>> languageVariants,
    @JsonProperty("libraries") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Library> libraries,
    @JsonProperty("magicwords") @JsonSetter(nulls = Nulls.AS_EMPTY) List<MagicWord> magicWords,
    @JsonProperty("namespacealiases") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<NamespaceAlias> namespaceAliases,
    @JsonProperty("namespaces") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, Namespace> namespaces,
    @JsonProperty("protocols") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> protocols,
    @JsonProperty("restrictions") @Nullable Restrictions restrictions,
    @JsonProperty("rightsinfo") @Nullable RightsInfo rightsInfo,
    @JsonProperty("showhooks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ShowHook> showHooks,
    @JsonProperty("skins") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Skin> skins,
    @JsonProperty("specialpagealiases") @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<SpecialPageAlias> specialPageAliases,
    @JsonProperty("statistics") @Nullable Statistics statistics,
    @JsonProperty("uploaddialog") @Nullable UploadDialog uploadDialog,
    @JsonProperty("usergroups") @JsonSetter(nulls = Nulls.AS_EMPTY) List<UserGroup> userGroups,
    @JsonProperty("variables") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> variables) {

  public static SiteInfo ofEmpty() {
    return new SiteInfo(
        List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), null, List.of(), List.of(),
        Map.of(), List.of(), List.of(), List.of(), Map.of(), List.of(), null, null, List.of(),
        List.of(), List.of(), null, null, List.of(), List.of());
  }
}
