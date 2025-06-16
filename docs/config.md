# Configuration

Clj-depend can be configured in two ways:

- Configuration file (`.clj-depend/config.edn`) in the `:project-root` directory.
- `:config` argument when using API.

The configurations are merged in the following order, where a later config overrides an earlier config:

- default config.
- project config.
- config argument.

## All settings

You can find all settings and its default values [here](../src/clj_depend/config.clj) and below the docs for each one:

### :source-paths

Directories within the project to look for clj files. Files outside the source-paths will be ignored.

Default: `#{"src"}`.

Config example:
```clojure
{,,,
 :source-paths #{"src" "test"}
 ,,,}
```

### :layers

Defining the layers of your project.

Default: `{}`.

A map where each key is a layer and the value is a map, where:
- The layer is defined by a regex using the `:defined-by` key or a set of namespaces using the `:namespaces` key.
- The accesses allowed by it declared using the `:accesses-layers` key, or the accesses that are allowed to the layer using the `:accessed-by-layers` key. Since both keys accept a set of layers.
- `:only-ns-in-source-paths` optional, only considers namespaces in source paths as part of a layer. Available values: `true`, `false` with default value of `false`.
- `:access-peer-ns` optional, controls whether namespaces within the same layer can access each other. Available values: `true`, `false` with default value of `true`. When `false`, namespaces in the same layer cannot depend on other namespaces in the same layer.

Config example:
```clojure
{,,,
 :layers {:controller {:defined-by      ".*\\.controller\\..*"
                       :accesses-layers #{:logic :model}
                       :access-peer-ns false}
          :logic      {:defined-by      ".*\\.logic\\..*"
                       :accesses-layers #{:model}}
          :model      {:defined-by              ".*\\.model\\..*"
                       :accesses-layers         #{}
                       :only-ns-in-source-paths true}}
 ,,,}
```

### :rules

Defining the rules for namespaces.

Default: `[]`.

A vector of rules whose each rule is a map composed of the following fields:
- `:defined-by` optional, a regular expression (regex) that serves as a predicate to identify whether the rule should be evaluated.
- `:namespaces` optional, a set of namespaces that serves as a predicate to identify whether the rule should be evaluated.
- `:should-not-depend-on` required, a set of namespaces or regular expressions (regex).
- `:message` optional, a custom violation message.

You can use the keys that serve as predicates (`:defined-by` and `:namespaces`) individually, combine both, or use neither. **If none of them are provided, the rule will apply to all namespaces present in the configured source-paths.**

Config example:
```clojure
{,,,
 :rules [{:defined-by           ".*\\.logic\\..*"
          :should-not-depend-on #{".*\\.controller\\..*"}}
         {:namespaces           #{foo.a}
          :should-not-depend-on #{foo.x}
          :message              "Prefer using foo.y instead of foo.x"}]
 ,,,}
```

