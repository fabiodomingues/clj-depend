(ns clj-depend.analyzer
  (:require [clj-depend.analyzers.circular-dependency :as analyzers.circular-dependency]
            [clj-depend.analyzers.layer :as analyzers.layer]
            [clj-depend.analyzers.rule :as analyzers.rule]))

(set! *warn-on-reflection* true)

(defn ^:private violations
  [config dependencies-by-namespace namespace]
  (let [circular-dependency-violations (analyzers.circular-dependency/analyze namespace dependencies-by-namespace)
        layer-violations (analyzers.layer/analyze config namespace dependencies-by-namespace)
        rule-violations (analyzers.rule/analyze config namespace dependencies-by-namespace)]
    (not-empty (concat circular-dependency-violations layer-violations rule-violations))))

(defn analyze
  "Analyze namespaces dependencies."
  [{:keys [config namespaces-to-be-analyzed namespaces-and-dependencies]}]
  (let [dependencies-by-namespace (reduce-kv (fn [m k v] (assoc m k (:dependencies (first v))))
                                             {}
                                             (group-by :namespace namespaces-and-dependencies))]
    (flatten (keep #(violations config dependencies-by-namespace %) namespaces-to-be-analyzed))))
