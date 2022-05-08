(ns clj-depend.dependency
  (:require [clojure.tools.namespace.dependency :as namespace.dependency]))

(defn dependencies-graph
  [namespaces]
  (reduce
    (fn [graph namespace]
      (reduce
        (fn [graph dep]
          (namespace.dependency/depend graph (:name namespace) dep))
        graph
        (:dependencies namespace)))
    (namespace.dependency/graph)
    namespaces))

(defn immediate-dependents
  [dependency-graph
   namespace]
  (namespace.dependency/immediate-dependents dependency-graph namespace))
