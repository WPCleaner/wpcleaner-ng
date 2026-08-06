package org.wpcleaner.api.api.query.list.random;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum RandomParameters {
  CONTENT_MODEL("rncontentmodel"),
  CONTINUE("rncontinue"),
  FILTER_REDIRECT("rnfilterredir"),
  LIMIT("rnlimit"),
  MAX_SIZE("rnmaxsize"),
  MIN_SIZE("rnminsize"),
  NAMESPACE("rnnamespace"),
  ;

  public final String value;

  RandomParameters(final String value) {
    this.value = value;
  }

  public enum ContentModel {
    CSS("css"),
    FLOW_BOARD("flow-board"),
    GADGET_DEFINITION("GadgetDefinition"),
    GRAPH_JSON_CONFIG("Graph.JsonConfig"),
    JAVASCRIPT("javascript"),
    JSON("json"),
    JSON_JSON_CONFIG("Json.JsonConfig"),
    JSON_SCHEMA("JsonSchema"),
    MASS_MESSAGE_LIST_CONTENT("MassMessageListContent"),
    NEWSLETTER_CONTENT("NewsletterContent"),
    SANITIZED_CSS("sanitized-css"),
    SCRIBUNTO("Scribunto"),
    SECURE_POLL("SecurePoll"),
    TEXT("text"),
    TRANSLATE_MESSAGE_BUNDLE("translate-messagebundle"),
    UNKNOWN("unknown"),
    VUE("vue"),
    WIKITEXT("wikitext"),
    WORKLIST("worklist"),
    ;

    public final String value;

    ContentModel(final String value) {
      this.value = value;
    }
  }

  public enum FilterRedirect {
    ALL("all"),
    NON_REDIRECTS("nonredirects"),
    REDIRECTS("redirects"),
    ;

    public final String value;

    FilterRedirect(final String value) {
      this.value = value;
    }
  }
}
