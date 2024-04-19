[![Clojars Project](http://clojars.org/com.fabiodomingues/clj-depend/latest-version.svg)](http://clojars.org/com.fabiodomingues/clj-depend)

# clj-depend

A Clojure namespace dependency analyzer.

## Usage

### Leiningen

To run clj-depend from Leiningen, check [lein-clj-depend](./lein-clj-depend/README.md) plugin.

### Clojure CLI (tools.deps)

Add clj-depend as a dependency, preferably under an alias in `deps.edn`.

```clojure
{:deps { ,,, }
 :aliases {:clj-depend {:extra-deps {com.fabiodomingues/clj-depend {:mvn/version "0.11.0"}}
                        :main-opts ["-m" "clj-depend.main"]}}}
```

Run:

```
clj -M:clj-depend
```

### API

You can use the namespace [clj-depend.api](https://cljdoc.org/d/com.fabiodomingues/clj-depend/CURRENT/api/clj-depend.api) to have access to all clj-depend features.

## Configuration

To let clj-depend know the existing layers in your application and the allowed dependencies between these layers, create a `.clj-depend` directory at the root of the project and inside it a `config.edn` file.

[More details](./docs/config.md).

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
{:source-paths #{"src"}
 :layers {:controller {:defined-by         ".*\\.controller\\..*"
                       :accesses-layers #{:logic :model}}
          :logic      {:defined-by         ".*\\.logic\\..*"
                       :accesses-layers #{:model}}
          :model      {:defined-by         ".*\\.model\\..*"
                       :accesses-layers #{}}}}
```

### Rule Checks

Example configuration file (`.clj-depend/config.edn`):

```clojure
{,,,
 :rules [{:defined-by           ".*\\.logic\\..*"
          :should-not-depend-on #{".*\\.controller\\..*"}}]
 ,,,}
```

### Circular Dependency Checks

Diagram to exemplify the circular dependency between namespaces:

```mermaid
graph TD
    A[foo.controllers.user] --> B[foo.controllers.customer]
    B --> A
```
