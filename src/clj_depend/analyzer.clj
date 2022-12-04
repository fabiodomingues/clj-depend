(ns clj-depend.analyzer
  (:require [clj-depend.dependency :as dependency]))

(defn- violate?
  [config
   {:keys [module dependency-module layer dependency-layer] :as x}]
  (or (and (not (nil? module)) (not (nil? dependency-module)) (not= module dependency-module))
      (and (not= layer dependency-layer)
           (when-let [accessed-by-layers (get-in config [:layers dependency-layer :accessed-by-layers])]
             (not-any? (partial = layer) accessed-by-layers)))))

(defn- namespace-belongs-to-layer?
  [config namespace layer]
  (let [namespaces (get-in config [:layers layer :namespaces])
        defined-by (get-in config [:layers layer :defined-by])]
    (or (some #{namespace}  namespaces)
        (when defined-by (re-find (re-pattern defined-by) (str namespace))))))

(defn- layer-by-namespace
  [config namespace]
  (some #(when (namespace-belongs-to-layer? config namespace %) %) (keys (:layers config))))

(defn- namespace-belongs-to-module?
  [config namespace layer]
  (let [defined-by (get-in config [:modules layer :defined-by])]
    (when defined-by (re-find (re-pattern defined-by) (str namespace)))))

(defn- module-by-namespace
  [config namespace]
  (some #(when (namespace-belongs-to-module? config namespace %) %) (keys (:modules config))))

(defn- layer-module-and-namespace [config namespace dependency-namespace]
  (let [module (module-by-namespace config namespace)
        layer (layer-by-namespace config namespace)]
    (when (or module layer)
      {:namespace            namespace
       :module               module
       :layer                layer
       :dependency-namespace dependency-namespace
       :dependency-module    (module-by-namespace config dependency-namespace)
       :dependency-layer     (layer-by-namespace config dependency-namespace)})))

(defn- violations
  [config dependency-graph namespace]
  (let [dependencies (dependency/immediate-dependencies dependency-graph namespace)]
    (->> dependencies
         (map #(layer-module-and-namespace config namespace %))
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
