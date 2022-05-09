(ns clj-depend.api 
  (:require [clj-depend.internal-api :as internal-api])
  (:import [java.io File]))

(defn analyze
  "Analyze namespaces dependencies.
   
   Takes a map with:

   - `:project-root` required, a java.io.File representing the project root.

   - `:source-paths` optional, a set of source paths (eg. `src`).

   - `:config` optional, a configuration map. Otherwise, it will try to resolve the configuration using `.clj-depend/config.edn` in `:project-root` directory.
   
   - `:namespaces` optional, a set with only namespaces that need to be analyzed in the project.

   Returns a map with `:violations`.

   **Example**

   ```clojure
   (clj-depend.api/analyze {:project-root (io/file \".\")
                            :source-paths #{\"src\"}
                            :config {:layers {:foo {:defined-by \"foo\\..*\" :accessed-by-layers #{}}
                                              :bar {:defined-by \"bar\\..*\" :accessed-by-layers #{:foo}}}}
                            :namespaces #{foo.x bar.y}})
   ```
   "
  [{:keys [project-root] :as options}]
  {:pre [(and (instance? File project-root)
              (.exists ^File project-root))]}
  (internal-api/analyze options))
