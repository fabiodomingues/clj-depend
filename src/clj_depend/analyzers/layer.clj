(ns clj-depend.analyzers.layer
  (:require [clojure.set :as set]))

(set! *warn-on-reflection* true)

(defn ^:private layer-cannot-access-dependency-layer?
  [config layer dependency-layer]
  (when-let [accesses-layers (get-in config [:layers layer :accesses-layers])]
    (not-any? (partial = dependency-layer) accesses-layers)))

(defn ^:private dependency-layer-cannot-be-accessed-by-layer?
  [config dependency-layer layer]
  (when-let [accessed-by-layers (get-in config [:layers dependency-layer :accessed-by-layers])]
    (not-any? (partial = layer) accessed-by-layers)))

(defn ^:private violate?
  [config
   {:keys [layer dependency-layer]}]
  (and (not= layer dependency-layer)
       (and (not (nil? layer))
            (not (nil? dependency-layer)))
       (or (dependency-layer-cannot-be-accessed-by-layer? config dependency-layer layer)
           (layer-cannot-access-dependency-layer? config layer dependency-layer))))

(defn ^:private namespace-in-source-paths?
  [namespace dependencies-by-namespace]
  (contains? (set (keys dependencies-by-namespace)) namespace))

(defn ^:private namespace-belongs-to-layer?
  [config namespace layer dependencies-by-namespace]
  (let [namespaces (get-in config [:layers layer :namespaces])
        defined-by (get-in config [:layers layer :defined-by])
        only-ns-in-source-paths (get-in config [:layers layer :only-ns-in-source-paths])]
    (and (or (not only-ns-in-source-paths)
             (and only-ns-in-source-paths (namespace-in-source-paths? namespace dependencies-by-namespace)))
         (or (some #{namespace} namespaces)
             (when defined-by (re-find (re-pattern defined-by) (str namespace)))))))

(defn ^:private layer-by-namespace
  [config namespace dependencies-by-namespace]
  (some #(when (namespace-belongs-to-layer? config namespace % dependencies-by-namespace) %) (keys (:layers config))))

(defn ^:private layer-and-namespace [config namespace dependency-namespace dependencies-by-namespace]
  (when-let [layer (layer-by-namespace config namespace dependencies-by-namespace)]
    {:namespace            namespace
     :layer                layer
     :dependency-namespace dependency-namespace
     :dependency-layer     (layer-by-namespace config dependency-namespace dependencies-by-namespace)}))

(defn ^:private namespace-dependencies
  [{:keys [only-ns-in-source-paths]} namespace dependencies-by-namespace]
  (let [namespace-dependencies (get dependencies-by-namespace namespace)]
    (if only-ns-in-source-paths
      (set/intersection (set namespace-dependencies) (set (keys dependencies-by-namespace)))
      namespace-dependencies)))

(defn analyze
  [config namespace dependencies-by-namespace]
  (->> (get dependencies-by-namespace namespace)
       (map #(layer-and-namespace config namespace % dependencies-by-namespace))
       (filter #(violate? config %))
       (map (fn [{:keys [namespace dependency-namespace layer dependency-layer] :as violation}]
              (assoc violation :message (str \" namespace \" " should not depend on " \" dependency-namespace \" " (layer " \" layer \" " on " \" dependency-layer \" ")"))))))
