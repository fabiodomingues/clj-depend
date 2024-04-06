(ns clj-depend.analyzers.circular-dependency)

(defn violations
  [namespace dependencies dependencies-by-namespace]
  (->> dependencies-by-namespace
       (filter (fn [[k _]] (contains? dependencies k)))
       (filter (fn [[_ v]] (contains? v namespace)))
       (map (fn [[k _]] {:namespace namespace :message (str "Circular dependency between " \" namespace \" " and " \" k \")}))))
