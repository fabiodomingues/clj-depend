(ns clj-depend.analyzer
  (:require [clj-depend.model.analyzer :as model.analyzer]
            [clj-depend.dependency :as dependency]
            [schema.core :as s]))

(defn layer-by-namespace
  [config namespace]
  (first (filter #(re-find (re-pattern (get-in config [:layers % :defined-by])) namespace) (keys (:layers config)))))

(defn violate?
  [config
   {:keys [layer dependent-layer]}]
  (let [accessed-by-layers (get-in config [:layers layer :accessed-by-layers])]
    (not (some #(= dependent-layer %) accessed-by-layers))))

(defn layer-and-namespace [config namespace dependent-namespace]
  (let [layer (layer-by-namespace config namespace)
        dependent-layer (layer-by-namespace config dependent-namespace)]
    {:namespace           namespace
     :dependent-namespace dependent-namespace
     :layer               layer
     :dependent-layer     dependent-layer}))

(defn violations
  [config dependency-graph namespace]
  (let [dependent-namespaces (dependency/transitive-dependents dependency-graph namespace)]
    (->> dependent-namespaces
         (map #(layer-and-namespace config namespace %))
         (filter #(violate? config %))
         not-empty)))

(s/defn analyze
  "Analyze namespaces dependencies."
  [{:keys [config namespaces]} :- model.analyzer/Context]
  (let [dependency-graph (dependency/dependencies-graph namespaces)
        violations (flatten (keep #(violations config dependency-graph (:name %)) namespaces))
        violations-by-namespace (->> (group-by :dependent-namespace violations)
                                     (map (fn [[key values]] {:namespace  key
                                                              :violations (map :namespace values)})))]
    (map (fn [{:keys [namespace dependent-namespace]}]
           {:namespace dependent-namespace
            :violation namespace}) violations)))