(ns clj-depend.dependency
  (:require [clojure.tools.namespace.dependency :as namespace.dependency])
  {:no-doc true})

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

(defn immediate-dependencies
  [dependency-graph
   namespace]
  (namespace.dependency/immediate-dependencies dependency-graph namespace))
