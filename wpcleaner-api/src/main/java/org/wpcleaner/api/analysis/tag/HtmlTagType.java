package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@SuppressWarnings("PMD.ExcessivePublicCount")
public enum HtmlTagType implements TagType {
  A("a", Type.REGULAR),
  ABBR("abbr", Type.REGULAR),
  AREA("area", Type.VOID),
  BASE("base", Type.VOID),
  B("b", Type.REGULAR),
  BIG("big", Type.REGULAR),
  BLOCKQUOTE("blockquote", Type.REGULAR),
  BR("br", Type.VOID),
  CENTER("center", Type.REGULAR),
  CITE("cite", Type.REGULAR),
  CODE("code", Type.REGULAR),
  COL("col", Type.VOID),
  COMMAND("command", Type.VOID),
  DEL("del", Type.REGULAR),
  DFN("dfn", Type.REGULAR),
  DIV("div", Type.REGULAR),
  EM("em", Type.REGULAR),
  EMBED("embed", Type.VOID),
  FONT("font", Type.REGULAR),
  H1("h1", Type.REGULAR),
  H2("h2", Type.REGULAR),
  H3("h3", Type.REGULAR),
  H4("h4", Type.REGULAR),
  H5("h5", Type.REGULAR),
  H6("h6", Type.REGULAR),
  H7("h7", Type.REGULAR),
  H8("h8", Type.REGULAR),
  H9("h9", Type.REGULAR),
  HR("hr", Type.VOID),
  I("i", Type.REGULAR),
  IMG("img", Type.VOID),
  INPUT("input", Type.VOID),
  KBD("kbd", Type.REGULAR),
  KEYGEN("keygen", Type.VOID),
  LI("li", Type.REGULAR),
  LINK("link", Type.VOID),
  META("meta", Type.VOID),
  OL("ol", Type.REGULAR),
  P("p", Type.REGULAR),
  PARAM("param", Type.VOID),
  S("s", Type.REGULAR),
  SAMP("samp", Type.REGULAR),
  SMALL("small", Type.REGULAR),
  SPAN("span", Type.REGULAR),
  STRIKE("strike", Type.REGULAR),
  STRONG("strong", Type.REGULAR),
  SUB("sub", Type.REGULAR),
  SUP("sup", Type.REGULAR),
  TABLE("table", Type.REGULAR),
  TD("td", Type.REGULAR),
  TH("th", Type.REGULAR),
  TR("tr", Type.REGULAR),
  TRACK("track", Type.VOID),
  U("u", Type.REGULAR),
  UL("ul", Type.REGULAR),
  VAR("var", Type.REGULAR),
  WBR("wbr", Type.VOID);

  private final String normalizedName;
  private final Type type;

  HtmlTagType(final String normalizedName, final Type type) {
    this.normalizedName = normalizedName;
    this.type = type;
    TagTypeRepository.registerTagType(this);
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
    return Type.REGULAR == type;
  }

  @Override
  public boolean canBeSelfClosing() {
    return true;
  }

  @Override
  public boolean isVoid() {
    return Type.VOID == type;
  }

  private enum Type {
    REGULAR,
    VOID
  }
}
