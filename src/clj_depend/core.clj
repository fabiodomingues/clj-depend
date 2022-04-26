(ns clj-depend.core
  (:require [clj-depend.config :as config]
            [clj-depend.analyzer :as analyzer]
            [clojure.tools.namespace.find :as namespace.find]
            [clojure.tools.namespace.parse :as namespace.parse]
            [clojure.java.io :as io]))

(defn- print!
  [analyzer-report duration]
  (let [violations-count (count analyzer-report)]
    (when (pos? violations-count)
      (println "Identified violations:")
      (doseq [{:keys [namespace violation]} analyzer-report]
        (println (str \" namespace \" " namespace depends on " violation))))
    (println (format "\nclj-depend took %sms, violations: %s" duration violations-count))))

(defn- parse-clojure-files!
  [dirs]
  (let [ns-decls (mapcat (fn [dir]
                           (namespace.find/find-ns-decls-in-dir (io/file dir))) dirs)]
    (map (fn [ns-decl]
           {:name         (str (namespace.parse/name-from-ns-decl ns-decl))
            :dependencies (map str (namespace.parse/deps-from-ns-decl ns-decl))}) ns-decls)))

(defn execute!
  "Analyze namespaces dependencies."
  [project-dir source-paths]
  (let [start-time      (System/currentTimeMillis)
        config          (config/read! project-dir)
        namespaces      (parse-clojure-files! source-paths)
        analyzer-report (analyzer/analyze config namespaces)
        duration        (- (System/currentTimeMillis) start-time)]
    (print! analyzer-report duration)
    (if (zero? (count analyzer-report))
      (System/exit 0)
      (System/exit 1))))
