package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

final class PageAnalysisTestFactory {

  static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  private PageAnalysisTestFactory() {}

  static PageAnalysis createPageAnalysis(final String text) {
    return createPageAnalysis(WIKI, text);
  }

  static PageAnalysis createPageAnalysis(final WikiDefinition wiki, final String text) {
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Catégories"),
            CaseType.FIRST_LETTER));
    final ProtocolRepository protocolRepository = new ProtocolRepository();
    protocolRepository.addProtocol(WIKI, new Protocol("https://"));
    protocolRepository.addProtocol(WIKI, new Protocol("http://"));
    final ExtensionTagRepository extensionTagRepository = new ExtensionTagRepository();
    Stream.of("nowiki", "pre", "ref", "references", "math", "inputbox")
        .forEach(name -> extensionTagRepository.addExtensionTag(WIKI, new ExtensionTag(name)));
    final MagicWordRepository magicWordRepository = new MagicWordRepository();
    Stream.of(
            new MagicWord(
                "currentyear", List.of("ANNEEACTUELLE", "CURRENTYEAR"), true, false, true),
            new MagicWord("defaultsort", List.of("CLEFDETRI:", "DEFAULTSORT:"), true, true, false),
            new MagicWord("if", List.of("si", "if"), false, true, false),
            new MagicWord("language", List.of("#LANGUE", "#LANGUAGE"), false, true, false),
            new MagicWord("lc", List.of("MINUS:", "LC:"), false, true, false),
            new MagicWord("pagename", List.of("NOMPAGE", "PAGENAME"), true, true, true))
        .forEach(magicWord -> magicWordRepository.addMagicWord(WIKI, magicWord));
    return new PageAnalysis(
        wiki,
        "Title",
        text,
        extensionTagRepository,
        new InterwikiRepository(),
        magicWordRepository,
        namespaceRepository,
        protocolRepository);
  }
}
