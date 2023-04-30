# CHANGELOG

## Unreleased

* [#33](https://github.com/fabiodomingues/clj-depend/issues/33): Merge default configuration, project configuration and configurations passed as parameter.
* [#28](https://github.com/fabiodomingues/clj-depend/issues/28): fix violation message from `should not depends on` to `should not depend on`.
* [#26](https://github.com/fabiodomingues/clj-depend/issues/26): add the `:accesses-layers` option to define the dependencies of a layer in the natural order instead of `:accessed-by-layers`.
* [#31](https://github.com/fabiodomingues/clj-depend/issues/31): fix regression reporting false positives for namespaces that are not covered by any other layer.
* [#27](https://github.com/fabiodomingues/clj-depend/issues/27): print violated layers.

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
