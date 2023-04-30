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

Directories within the project to look for clj files.

Default: `#{"src"}`.

### :layers

Defining the layers of your project.

Default: `{}`.

A map where each key is a layer and the value is a map, where:
- The layer is defined by a regex using the `:defined-by` key or a set of namespaces using the `:namespaces` key.
- The accesses allowed by it declared using the `:accesses-layers` key, or the accesses that are allowed to the layer using the `:accessed-by-layers` key. Since both keys accept a set of layers.

Layer configuration example:
```clojure
{:controller {:defined-by         ".*\\.controller\\..*"
              :accesses-layers #{:logic :model}}
 :logic      {:defined-by         ".*\\.logic\\..*"
              :accesses-layers #{:model}}
 :model      {:defined-by         ".*\\.model\\..*"
              :accesses-layers #{}}}
```
