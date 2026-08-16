package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.NamespaceAlias;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;

@Service
public class NamespaceExtractor {

  private final NamespaceRepository repository;

  public NamespaceExtractor(final NamespaceRepository repository) {
    this.repository = repository;
  }

  public void extract(final SiteInfo siteInfo) {
    siteInfo.namespaces().values().forEach(namespace -> extractNamespace(siteInfo, namespace));
  }

  private void extractNamespace(
      final SiteInfo siteInfo,
      final org.wpcleaner.api.api.query.meta.siteinfo.Namespace namespace) {
    final String name =
        Objects.requireNonNullElseGet(
            namespace.name(), () -> Objects.requireNonNullElse(namespace.local(), ""));
    final String canonical = Objects.requireNonNullElse(namespace.canonical(), "");
    repository.addNamespace(
        new Namespace(
            namespace.id(),
            canonical,
            name,
            extractNamespaceAliases(siteInfo, namespace.id()),
            CaseType.fromValue(namespace.caseType())));
  }

  private List<String> extractNamespaceAliases(final SiteInfo siteInfo, final int id) {
    return siteInfo.namespaceAliases().stream()
        .filter(alias -> alias.id() == id)
        .map(NamespaceAlias::alias)
        .sorted()
        .toList();
  }
}
