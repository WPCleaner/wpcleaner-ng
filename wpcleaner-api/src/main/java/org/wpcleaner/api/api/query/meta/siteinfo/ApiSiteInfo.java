package org.wpcleaner.api.api.query.meta.siteinfo;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiSiteInfo {

  private final ApiRestClient restClient;

  public ApiSiteInfo(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public SiteInfo requestSiteInfo(
      final WikiDefinition wiki,
      final Collection<SiteInfoParameters.Properties> properties,
      @Nullable final SiteInfoParameters.FilterInterwiki filterInterwiki,
      @Nullable final Boolean showImageList,
      @Nullable final Boolean numberInGroup) {
    return Optional.ofNullable(
            internalRequestSiteInfo(
                wiki, properties, filterInterwiki, showImageList, numberInGroup))
        .map(Response::query)
        .map(
            q ->
                new SiteInfo(
                    q.dbReplicationLag(),
                    q.defaultOptions(),
                    q.extensions(),
                    q.extensionTags(),
                    q.fileExtensions(),
                    q.functionHooks(),
                    q.general(),
                    q.interwikiMap(),
                    q.languages(),
                    q.languageVariants(),
                    q.libraries(),
                    q.magicWords(),
                    q.namespaceAliases(),
                    q.namespaces(),
                    q.protocols(),
                    q.restrictions(),
                    q.rightsInfo(),
                    q.showHooks(),
                    q.skins(),
                    q.specialPageAliases(),
                    q.statistics(),
                    q.uploadDialog(),
                    q.userGroups(),
                    q.variables()))
        .orElseGet(SiteInfo::ofEmpty);
  }

  @Nullable
  private Response internalRequestSiteInfo(
      final WikiDefinition wiki,
      final Collection<SiteInfoParameters.Properties> properties,
      @Nullable final SiteInfoParameters.FilterInterwiki filterInterwiki,
      @Nullable final Boolean showImageList,
      @Nullable final Boolean numberInGroup) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(
            uriBuilder ->
                computeUri(uriBuilder, properties, filterInterwiki, showImageList, numberInGroup))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(
      final UriBuilder uriBuilder,
      final Collection<SiteInfoParameters.Properties> properties,
      @Nullable final SiteInfoParameters.FilterInterwiki filterInterwiki,
      @Nullable final Boolean showImageList,
      @Nullable final Boolean numberInGroup) {
    UriBuilder builder =
        ApiUtils.configure(uriBuilder, ApiParameters.Action.QUERY)
            .queryParam(QueryParameters.META.value, QueryParameters.Meta.SITE_INFO.value);

    if (properties != null && !properties.isEmpty()) {
      builder =
          builder.queryParam(
              SiteInfoParameters.PROPERTIES.value,
              properties.stream().map(prop -> prop.value).collect(Collectors.joining("|")));
    }
    if (filterInterwiki != null) {
      builder =
          builder.queryParam(SiteInfoParameters.FILTER_INTERWIKI.value, filterInterwiki.value);
    }
    if (showImageList != null) {
      builder =
          builder.queryParam(SiteInfoParameters.SHOW_IMAGE_LIST.value, showImageList.toString());
    }
    if (numberInGroup != null) {
      builder =
          builder.queryParam(SiteInfoParameters.NUMBER_IN_GROUP.value, numberInGroup.toString());
    }
    return builder.build();
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("dbrepllag") @JsonSetter(nulls = Nulls.AS_EMPTY) List<DbReplicationLag> dbReplicationLag,
      @JsonProperty("defaultoptions") @JsonSetter(nulls = Nulls.AS_EMPTY)
          Map<String, Object> defaultOptions,
      @JsonProperty("extensions") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Extension> extensions,
      @JsonProperty("extensiontags") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> extensionTags,
      @JsonProperty("fileextensions") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<FileExtension> fileExtensions,
      @JsonProperty("functionhooks") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> functionHooks,
      @JsonProperty("general") @Nullable General general,
      @JsonProperty("interwikimap") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<Interwiki> interwikiMap,
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
      @JsonProperty("variables") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> variables) {}
}
