# CHANGELOG

## Unreleased

## 0.11.0 (2024-04-19)
* [#56](https://github.com/fabiodomingues/clj-depend/issues/56): Added the `:only-ns-in-source-paths` attribute for when it is necessary to consider only namespaces in source paths as part of a layer.

## 0.10.0 (2024-04-08)
* [#51](https://github.com/fabiodomingues/clj-depend/issues/51): Namespace rule analyzer.
* [#55](https://github.com/fabiodomingues/clj-depend/issues/55): Split analyzers into separate namespaces for better code organization and consequently simplify testing.
* [#48](https://github.com/fabiodomingues/clj-depend/issues/48): Fix analyzing files outside of configured source-paths.

## 0.9.2 (2023-10-24)
* [#46](https://github.com/fabiodomingues/clj-depend/issues/46): Fix false violation when using `:access-layers` due to dependency on namespaces not defined in layers.

## 0.9.1 (2023-10-24)
* [#44](https://github.com/fabiodomingues/clj-depend/issues/44): Fix not creating snapshot file when clj-depend folder does not exist.

## 0.9.0 (2023-09-24)
* [#41](https://github.com/fabiodomingues/clj-depend/issues/41): API to check if clj-depend is configured.
* [#39](https://github.com/fabiodomingues/clj-depend/issues/39): Fix circular dependency not being treated as a violation.

## 0.8.1 (2023-09-04)
* Fix snapshot file name.
* Fix documentation (cljdoc).

## 0.8.0 (2023-09-03)
* [#38](https://github.com/fabiodomingues/clj-depend/issues/38): Fix execution failure when receiving arguments from Leiningen.

## 0.7.0 (2023-09-03)
* [#1](https://github.com/fabiodomingues/clj-depend/issues/1): Dump the violations into a snapshot file (`.clj-depend/violations.edn`), and ignore any violations that are present in the snapshot file in future analysis.
* [#33](https://github.com/fabiodomingues/clj-depend/issues/33): Merge default configuration, project configuration and configurations passed as parameter.
* [#28](https://github.com/fabiodomingues/clj-depend/issues/28): Fix violation message from `should not depends on` to `should not depend on`.
* [#26](https://github.com/fabiodomingues/clj-depend/issues/26): Add the `:accesses-layers` option to define the dependencies of a layer in the natural order instead of `:accessed-by-layers`.
* [#31](https://github.com/fabiodomingues/clj-depend/issues/31): Fix regression reporting false positives for namespaces that are not covered by any other layer.
* [#27](https://github.com/fabiodomingues/clj-depend/issues/27): Print violated layers.

## 0.6.0 (2022-06-01)
* Fix violation when namespace depend on another on the same layer.
* Declaring namespaces directly in layer configuration without regex.

## 0.5.0 (2022-05-14)
* Documentation improvements (cljdoc).

## 0.4.0 (2022-05-14)
* Created an API for clj-depend.

## 0.3.0 (2022-05-09)
* Fixed analyzer to only consider immediate dependencies for each namespace.
* Extracted lein plugin to another project (lein-clj-depend).

## 0.2.0 (2022-04-27)
* Improve exit codes.

## 0.1.0 (2022-04-26)
* First release deployed to clojars.
