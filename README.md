[![Clojars Project](http://clojars.org/com.fabiodomingues/clj-depend/latest-version.svg)](http://clojars.org/com.fabiodomingues/clj-depend)

# clj-depend

A Clojure namespace dependency analyzer.

> ⚠️ Not ready, still in development.

## Usage

Add `[com.fabiodomingues/clj-depend "0.1.0"]` to `:plugins`.

```
$ lein clj-depend
```

If any cyclic dependency is found, the analysis will fail and an error message like this example will be displayed: `Circular dependency between "foo" and "bar"`.

## Configuration

To let clj-depend know the existing layers in your application and the allowed dependencies between these layers, create a `.clj-depend` directory at the root of the project and inside it a `config.edn` file.

### Layer Checks

Diagram to exemplify the dependency between layers:

```mermaid
graph TD
    A[foo.controller] --> B[foo.logic]
    A --> C[foo.model]
    B --> C
```

Configuration file (`.clj-depend/config.edn`) for diagram above:

```clojure
{:layers {:controller {:defined-by         ".*\\.controller\\..*"
                       :accessed-by-layers #{}}
          :logic      {:defined-by         ".*\\.logic\\..*"
                       :accessed-by-layers #{:controller}}
          :model      {:defined-by         ".*\\.model\\..*"
                       :accessed-by-layers #{:logic :controller}}}}
```
