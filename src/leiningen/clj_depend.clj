(ns leiningen.clj-depend
  (:require [clj-depend.util :as util]
            [clj-depend.analyzer :as analyzer]
            [clj-depend.core :as core]
            [clj-depend.util.system :as util.system])
  (:use [clojure.pprint]))

(defn clj-depend
  "Analyze namespaces dependencies."
  [project & args]
  (let [start-time (System/currentTimeMillis)
        config (util/read-config (:root project))
        source-path (:source-paths project)
        namespaces (util/parse-clojure-files source-path)
        analyzer-report (analyzer/analyze {:config     config
                                           :namespaces namespaces})
        duration (- (System/currentTimeMillis) start-time)]
    (core/print! analyzer-report duration)
    (if (= 0 (count analyzer-report))
      (util.system/exit 0)
      (util.system/exit 1))))
