package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;

@Service
public class NamespaceExtractor {

  private final NamespaceRepository repository;

  public NamespaceExtractor(final NamespaceRepository repository) {
    this.repository = repository;
  }

  public void extract(final SiteInfo siteInfo) {
    Optional.ofNullable(siteInfo.namespaces()).map(Map::values).stream()
        .flatMap(Collection::stream)
        .forEach(
            namespace -> {
              final String name =
                  Objects.requireNonNullElseGet(
                      namespace.name(), () -> Objects.requireNonNullElse(namespace.local(), ""));
              final String canonical = Objects.requireNonNullElse(namespace.canonical(), "");
              repository.addNamespace(new Namespace(namespace.id(), canonical, name));
            });
  }
}
