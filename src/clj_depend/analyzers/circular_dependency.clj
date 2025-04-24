(ns clj-depend.analyzers.circular-dependency)

(set! *warn-on-reflection* true)

(defn analyze
  [namespace dependencies-by-namespace]
  (let [current-namespace-dependencies (get dependencies-by-namespace namespace)]
    (->> dependencies-by-namespace
         (filter (fn [[k _]] (contains? current-namespace-dependencies k)))
         (filter (fn [[_ v]] (contains? v namespace)))
         (map (fn [[k _]] {:namespace namespace :dependency-namespace k :message (str "Circular dependency between " \" namespace \" " and " \" k \")})))))
