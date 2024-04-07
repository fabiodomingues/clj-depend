(ns clj-depend.analyzer
  (:require [clj-depend.analyzers.circular-dependency :as analyzers.circular-dependency]
            [clj-depend.analyzers.layer :as analyzers.layer]))

(defn- violations
  [config dependencies-by-namespace namespace]
  (let [dependencies (get dependencies-by-namespace namespace)
        circular-dependency-violations (analyzers.circular-dependency/analyze namespace dependencies dependencies-by-namespace)
        layer-violations (analyzers.layer/analyze config namespace dependencies)]
    (not-empty (concat circular-dependency-violations layer-violations))))

(defn analyze
  "Analyze namespaces dependencies."
  [{:keys [config dependencies-by-namespace]}]
  (flatten (keep #(violations config dependencies-by-namespace %) (keys dependencies-by-namespace))))
