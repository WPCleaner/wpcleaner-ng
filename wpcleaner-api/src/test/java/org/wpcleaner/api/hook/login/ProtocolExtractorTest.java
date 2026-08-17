package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

class ProtocolExtractorTest {

  @DisplayName("Should extract protocols from SiteInfo into ProtocolRepository")
  @Test
  void extract() {
    // GIVEN
    final ProtocolRepository repository = new ProtocolRepository();
    final ProtocolExtractor extractor = new ProtocolExtractor(repository);

    final SiteInfo siteInfo =
        new SiteInfo(
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null,
            List.of(),
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            Map.of(),
            List.of("http://", "https://"),
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            null,
            List.of(),
            List.of());

    // WHEN
    extractor.extract(siteInfo);

    // THEN
    final List<Protocol> protocols = repository.getProtocols();
    Assertions.assertThat(protocols).hasSize(2);
    Assertions.assertThat(protocols.get(0).value()).isEqualTo("http://");
    Assertions.assertThat(protocols.get(1).value()).isEqualTo("https://");
  }
}
