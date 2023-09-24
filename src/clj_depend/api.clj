(ns clj-depend.api
  (:require [clj-depend.internal-api :as internal-api])
  (:import [java.io File]))

(defn configured?
  "Given the project root directory, check if it has the configuration file (`.clj-depend/config.edn`).

  ```clojure
  (when (clj-depend.api/configured? (io/file \".\"))
    (do-something))
  ```"
  [project-root]
  {:pre [(and (instance? File project-root)
              (.exists ^File project-root))]}
  (internal-api/configured? project-root))

(defn analyze
  "Analyze namespaces dependencies.

  Takes a map with:

  - `:project-root` required, a java.io.File representing the project root directory.

  - `:config` optional, a configuration map that will be merged with default configs and configuration file (`.clj-depend/config.edn`) when present in the `:project-root` directory.

  - `:files` optional, a set of files or directories (java.io.File) representing files to be analyzed. If empty, all files in source paths declared in config will be considered.

  - `:namespaces` optional, a set of symbols representing namespaces to be analyzed. If empty, all project namespaces will be considered.

  - `:snapshot?` a boolean, when enable analyze namespace dependencies and dump the violations into a snapshot file (`.clj-depend/snapshot.edn`) that is used as a reference for further analysis.

  Returns a map with:

  - `:result-code` required, an integer:
    - 0: no violations were found
    - 1: one or more violations were found
    - 2: error during analysis
  - `:message` required, a string with message.
  - `:violations` a set of maps with `:namespace` and `:violation`.

  **Example**

  ```clojure
  (clj-depend.api/analyze {:project-root (io/file \".\")
                           :config {:source-paths #{\"src\"
                                    :layers {:foo {:defined-by \"foo\\..*\" :accesses-layers #{:bar}}
                                             :bar {:defined-by \"bar\\..*\" :accesses-layers #{}}}}}
                           :namespaces #{foo.x bar.y}})
  ```"
  [{:keys [project-root config files namespaces] :as options}]
  {:pre [(and (instance? File project-root)
              (.exists ^File project-root))
         (or (nil? config)
             (map? config))
         (or (nil? files)
             (set? files))
         (or (nil? namespaces)
             (set? namespaces))]}
  (internal-api/analyze options))
