package org.wpcleaner.api.api.query.list.search;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum SearchParameters {
  ENABLE_REWRITES("srenablerewrites"),
  INFO("srinfo"),
  INTERWIKI("srinterwiki"),
  LIMIT("srlimit"),
  NAMESPACE("srnamespace"),
  OFFSET("sroffset"),
  PROPERTIES("srprop"),
  QUERY_DEPENDENT_PROFILE("srqdprofile"),
  QUERY_INDEPENDENT_PROFILE("srqiprofile"),
  SEARCH("srsearch"),
  SORT("srsort"),
  WHAT("srwhat"),
  ;

  public final String value;

  SearchParameters(final String value) {
    this.value = value;
  }

  public enum Info {
    REWRITTEN_QUERY("rewrittenquery"),
    SUGGESTION("suggestion"),
    TOTAL_HITS("totalhits"),
    ;

    public final String value;

    Info(final String value) {
      this.value = value;
    }
  }

  public enum Properties {
    CATEGORY_SNIPPET("categorysnippet"),
    EXTENSION_DATA("extensiondata"),
    IS_FILE_MATCH("isfilematch"),
    REDIRECT_SNIPPET("redirectsnippet"),
    REDIRECT_TITLE("redirecttitle"),
    SECTION_SNIPPET("sectionsnippet"),
    SECTION_TITLE("sectiontitle"),
    SIZE("size"),
    SNIPPET("snippet"),
    TIMESTAMP("timestamp"),
    TITLE_SNIPPET("titlesnippet"),
    WORD_COUNT("wordcount"),
    ;

    public final String value;

    Properties(final String value) {
      this.value = value;
    }
  }

  public enum QueryDependentProfile {
    DEFAULT("default"),
    ENGINE_AUTOSELECT("engine_autoselect"),
    PERFIELD_BUILDER("perfield_builder"),
    PERFIELD_BUILDER_RELAXED("perfield_builder_relaxed"),
    PERFIELD_BUILDER_TITLE_FILTER("perfield_builder_title_filter"),
    ;

    public final String value;

    QueryDependentProfile(final String value) {
      this.value = value;
    }
  }

  public enum QueryIndependentProfile {
    CLASSIC("classic"),
    CLASSIC_NO_BOOST_LINKS("classic_noboostlinks"),
    EMPTY("empty"),
    ENGINE_AUTOSELECT("engine_autoselect"),
    POPULAR_INCOMING_LINKS("popular_inclinks"),
    POPULAR_INCOMING_LINKS_PAGE_VIEWS("popular_inclinks_pv"),
    WEIGHTED_SUM_INCOMING_LINKS("wsum_inclinks"),
    WEIGHTED_SUM_INCOMING_LINKS_PAGE_VIEWS("wsum_inclinks_pv"),
    ;

    public final String value;

    QueryIndependentProfile(final String value) {
      this.value = value;
    }
  }

  public enum Sort {
    CREATE_TIMESTAMP_ASC("create_timestamp_asc"),
    CREATE_TIMESTAMP_DESC("create_timestamp_desc"),
    INCOMING_LINKS_ASC("incoming_links_asc"),
    INCOMING_LINKS_DESC("incoming_links_desc"),
    JUST_MATCH("just_match"),
    LAST_EDIT_ASC("last_edit_asc"),
    LAST_EDIT_DESC("last_edit_desc"),
    NONE("none"),
    RANDOM("random"),
    RELEVANCE("relevance"),
    TITLE_NATURAL_ASC("title_natural_asc"),
    TITLE_NATURAL_DESC("title_natural_desc"),
    USER_RANDOM("user_random"),
    ;

    public final String value;

    Sort(final String value) {
      this.value = value;
    }
  }

  public enum What {
    NEAR_MATCH("nearmatch"),
    TEXT("text"),
    TITLE("title"),
    ;

    public final String value;

    What(final String value) {
      this.value = value;
    }
  }
}
