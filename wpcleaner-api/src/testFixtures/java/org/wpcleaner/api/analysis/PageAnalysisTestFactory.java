package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

public final class PageAnalysisTestFactory {

  public static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String DEFAULT_TITLE = "Title";

  private PageAnalysisTestFactory() {}

  public static PageAnalysis createPageAnalysis(final String text) {
    return createPageAnalysis(WIKI, DEFAULT_TITLE, text);
  }

  public static PageAnalysis createPageAnalysis(final String title, final String text) {
    return createPageAnalysis(WIKI, title, text);
  }

  public static PageAnalysis createPageAnalysis(final WikiDefinition wiki, final String text) {
    return createPageAnalysis(wiki, DEFAULT_TITLE, text);
  }

  public static PageAnalysis createPageAnalysis(
      final WikiDefinition wiki, final String title, final String text) {
    return createPageAnalysisFactory().analysis(wiki, title, text);
  }

  public static PageAnalysisFactory createPageAnalysisFactory() {
    return new PageAnalysisFactory(
        createExtensionTagRepository(),
        createInterwikiRepository(),
        createMagicLingRepository(),
        createMagicWordRepository(),
        createNamespaceRepository(),
        createProtocolRepository());
  }

  private static @NonNull InterwikiRepository createInterwikiRepository() {
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("en", false, "https://en.wikipedia.org/wiki/$1", "English", null));
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("wikt", false, "https://fr.wiktionary.org/wiki/$1", null, null));
    return interwikiRepository;
  }

  private static @NonNull NamespaceRepository createNamespaceRepository() {
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Catégories"),
            CaseType.FIRST_LETTER));
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.FILE.id, "File", "Fichier", List.of("Image"), CaseType.FIRST_LETTER));
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.USER.id,
            "User",
            "Utilisateur",
            List.of("Utilisatrice"),
            CaseType.FIRST_LETTER));
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.USER_TALK.id,
            "User talk",
            "Discussion utilisateur",
            List.of("Discussion utilisatrice"),
            CaseType.FIRST_LETTER));
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.TEMPLATE.id, "Template", "Modèle", List.of(), CaseType.FIRST_LETTER));
    return namespaceRepository;
  }

  private static @NonNull ProtocolRepository createProtocolRepository() {
    final ProtocolRepository protocolRepository = new ProtocolRepository();
    protocolRepository.addProtocol(WIKI, new Protocol("https://"));
    protocolRepository.addProtocol(WIKI, new Protocol("http://"));
    return protocolRepository;
  }

  private static @NonNull ExtensionTagRepository createExtensionTagRepository() {
    final ExtensionTagRepository extensionTagRepository = new ExtensionTagRepository();
    Stream.of(
            "nowiki",
            "pre",
            "ref",
            "references",
            "math",
            "inputbox",
            "gallery",
            "imagemap",
            "section")
        .forEach(name -> extensionTagRepository.addExtensionTag(WIKI, new ExtensionTag(name)));
    return extensionTagRepository;
  }

  private static @NonNull MagicLinkRepository createMagicLingRepository() {
    final MagicLinkRepository magicLinkRepository = new MagicLinkRepository();
    magicLinkRepository.enableMagicLink(WIKI, MagicLinkType.ISBN);
    magicLinkRepository.enableMagicLink(WIKI, MagicLinkType.PMID);
    return magicLinkRepository;
  }

  private static @NonNull MagicWordRepository createMagicWordRepository() {
    final MagicWordRepository magicWordRepository = new MagicWordRepository();
    Stream.of(
            new MagicWord(
                "currentyear", List.of("ANNEEACTUELLE", "CURRENTYEAR"), true, false, false, true),
            new MagicWord(
                "defaultsort", List.of("CLEFDETRI:", "DEFAULTSORT:"), true, false, true, false),
            new MagicWord("if", List.of("si", "if"), false, false, true, false),
            new MagicWord("img_alt", List.of("alt=$1"), true, false, false, false),
            new MagicWord("img_link", List.of("lien=$1", "link=$1"), true, false, false, false),
            new MagicWord("img_right", List.of("droite", "right"), true, false, false, false),
            new MagicWord(
                "img_thumbnail",
                List.of("vignette", "thumb", "thumbnail"),
                true,
                false,
                false,
                false),
            new MagicWord(
                "img_upright",
                List.of("redresse", "redresse=$1", "upright", "upright=$1"),
                true,
                false,
                false,
                false),
            new MagicWord("img_width", List.of("$1px"), true, false, false, false),
            new MagicWord("language", List.of("#LANGUE", "#LANGUAGE"), false, false, true, false),
            new MagicWord(
                "notoc", List.of("__AUCUNSOMMAIRE__", "__NOTOC__"), false, true, false, false),
            new MagicWord("toc", List.of("__SOMMAIRE__", "__TOC__"), false, true, false, false),
            new MagicWord("msg", List.of("MSG:"), false, false, false, false),
            new MagicWord("msgnw", List.of("MSGNW:"), false, false, false, false),
            new MagicWord("raw", List.of("RAW:"), false, false, false, false),
            new MagicWord("safesubst", List.of("SAFESUBST:"), false, false, false, false),
            new MagicWord("subst", List.of("SUBST:"), false, false, false, false),
            new MagicWord("lc", List.of("MINUS:", "LC:"), false, false, true, false),
            new MagicWord("lst", List.of("lst", "section"), false, false, true, false),
            new MagicWord("pagename", List.of("NOMPAGE", "PAGENAME"), true, false, true, true),
            new MagicWord(
                "redirect", List.of("#REDIRECTION", "#REDIRECT"), false, false, false, false))
        .forEach(magicWord -> magicWordRepository.addMagicWord(WIKI, magicWord));
    return magicWordRepository;
  }
}
