(ns leiningen.clj-depend
  (:require [clj-depend.core :as core]))

(defn clj-depend
  [project & args]
  (let [project-dir (:root project)
        source-paths (:source-paths project)]
    (core/execute! project-dir source-paths)))
