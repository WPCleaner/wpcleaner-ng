package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public enum WikiTagType implements TagType {
  CE("ce", Content.NON_WIKI),
  CHEM("chem", Content.NON_WIKI),
  GALLERY("gallery", Content.WIKI),
  GRAPH("graph", Content.NON_WIKI),
  HIERO("hiero", Content.NON_WIKI),
  IMAGEMAP("imagemap", Content.WIKI),
  INCLUDEONLY("includeonly", Content.WIKI),
  MAPFRAME("mapframe", Content.NON_WIKI),
  MATH("math", Content.NON_WIKI),
  NOINCLUDE("noinclude", Content.WIKI),
  NOWIKI("nowiki", Content.NON_WIKI),
  ONLYINCLUDE("onlyinclude", Content.WIKI),
  PRE("pre", Content.NON_WIKI),
  REF("ref", Content.WIKI),
  REFERENCES("references", Content.WIKI),
  SCORE("score", Content.NON_WIKI),
  SECTION("section", Content.WIKI),
  SOURCE("source", Content.NON_WIKI),
  SYNTAXHIGHLIGHT("syntaxhighlight", Content.NON_WIKI),
  TEMPLATEDATA("templatedata", Content.NON_WIKI),
  TIMELINE("timeline", Content.NON_WIKI);

  private final String normalizedName;
  private final Content content;

  WikiTagType(final String normalizedName, final Content content) {
    this.normalizedName = normalizedName;
    this.content = content;
  }

  @Override
  public String getNormalizedName() {
    return normalizedName;
  }

  @Override
  public boolean canBeStart() {
    return true;
  }

  @Override
  public boolean canBeEnd() {
    return true;
  }

  @Override
  public boolean canBeSelfClosing() {
    return true;
  }

  @Override
  public boolean isVoid() {
    return false;
  }

  @Override
  public boolean hasWikiContent() {
    return Content.WIKI == content;
  }

  private enum Content {
    WIKI,
    NON_WIKI
  }
}
