(ns clj-depend.analyzer
  (:require [clj-depend.dependency :as dependency]))

(defn- layer-by-namespace
  [config namespace]
  (first (filter #(re-find (re-pattern (get-in config [:layers % :defined-by])) (str namespace)) (keys (:layers config)))))

(defn- violate?
  [config
   {:keys [layer dependency-layer]}]
  (and (not= layer dependency-layer)
       (when-let [accessed-by-layers (get-in config [:layers dependency-layer :accessed-by-layers])]
         (not-any? (partial = layer) accessed-by-layers))))

(defn- layer-and-namespace [config namespace dependency-namespace]
  (when-let [layer (layer-by-namespace config namespace)]
    {:namespace            namespace
     :layer                layer
     :dependency-namespace dependency-namespace
     :dependency-layer     (layer-by-namespace config dependency-namespace)}))

(defn- violations
  [config dependency-graph namespace]
  (let [dependencies (dependency/immediate-dependencies dependency-graph namespace)]
    (->> dependencies
         (map #(layer-and-namespace config namespace %))
         (filter #(violate? config %))
         not-empty)))

(defn analyze
  "Analyze namespaces dependencies."
  [{:keys [config namespaces dependency-graph]}]
  (let [violations (flatten (keep #(violations config dependency-graph %) namespaces))]
    (map (fn [{:keys [namespace dependency-namespace]}]
           {:namespace namespace
            :dependency-namespace dependency-namespace
            :message (str \" namespace \" " should not depends on " \" dependency-namespace \")})
         violations)))
