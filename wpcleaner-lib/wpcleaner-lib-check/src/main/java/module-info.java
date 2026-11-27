/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@org.jspecify.annotations.NullMarked
module org.wpcleaner.lib.check {
  requires transitive org.jspecify;
  requires transitive org.wpcleaner.api;
  requires transitive spring.context;
  requires org.yaml.snakeyaml;
  exports org.wpcleaner.lib.check.algorithm;
  exports org.wpcleaner.lib.check.configuration;
  exports org.wpcleaner.lib.check.formatting;
  exports org.wpcleaner.lib.check.server;
  exports org.wpcleaner.lib.check;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a001;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a003;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a004;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a005;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a006;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a007;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a008;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a00x.a009;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a011;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a012;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a013;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a014;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a015;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a017;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a018;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a01x.a019;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a020;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a021;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a022;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a023;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a024;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a025;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a026;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a027;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a028;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a02x.a029;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a03x.a030;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a03x.a031;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a03x.a032;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a03x.a039;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a04x.a040;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a04x.a041;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a04x.a048;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a05x.a050;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a06x.a061;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a06x.a064;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a06x.a067;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a08x.a086;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a08x.a087;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a09x.a090;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a09x.a091;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a09x.a098;
  opens org.wpcleaner.lib.check.algorithm.a0xx.a09x.a099;
  opens org.wpcleaner.lib.check.algorithm.a1xx.a10x.a109;
  opens org.wpcleaner.lib.check.algorithm.a1xx.a11x.a110;
  opens org.wpcleaner.lib.check.algorithm.a5xx.a56x.a566;
  opens org.wpcleaner.lib.check.algorithm;
  opens org.wpcleaner.lib.check.configuration;
  opens org.wpcleaner.lib.check.formatting;
  opens org.wpcleaner.lib.check.server;
  opens org.wpcleaner.lib.check;
}
