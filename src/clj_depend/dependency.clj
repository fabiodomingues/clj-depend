(ns clj-depend.dependency
  (:require [clojure.tools.namespace.dependency :as namespace.dependency]
            [clj-depend.model.namespace :as model.namespace]
            [schema.core :as s]))

(s/defn dependencies-graph
  "Return a `clojure.tools.namespace` dependency graph of namespaces named by `ns-symbol`."
  [namespaces :- [model.namespace/Namespace]]
  (reduce
    (fn [graph namespace]
      (reduce
        (fn [graph dep]
          (namespace.dependency/depend graph (:name namespace) dep))
        graph
        (:dependencies namespace)))
    (namespace.dependency/graph)
    namespaces))

(s/defn transitive-dependents
  [dependency-graph
   namespace :- s/Str]
  (namespace.dependency/transitive-dependents dependency-graph namespace))
