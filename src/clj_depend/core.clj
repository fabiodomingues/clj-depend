(ns clj-depend.core
  (:require [clj-depend.config :as config]
            [clj-depend.analyzer :as analyzer]
            [clojure.tools.namespace.find :as namespace.find]
            [clojure.tools.namespace.parse :as namespace.parse]
            [clojure.java.io :as io]))

(defn- print!
  [analyzer-report]
  (let [violations-count (count analyzer-report)]
    (println (format "Identified %d violations" violations-count))
    (when (pos? violations-count)
      (doseq [{:keys [namespace violation]} analyzer-report]
        (println (str "- " \" namespace \" " depends on " \" violation \"))))))

(defn- parse-clojure-files!
  [dirs]
  (let [ns-decls (mapcat (fn [dir]
                           (namespace.find/find-ns-decls-in-dir (io/file dir))) dirs)]
    (map (fn [ns-decl]
           {:name         (str (namespace.parse/name-from-ns-decl ns-decl))
            :dependencies (map str (namespace.parse/deps-from-ns-decl ns-decl))}) ns-decls)))

(defn exit!
  [code]
  (System/exit code))

(defn execute!
  "Analyze namespaces dependencies."
  [project-dir source-paths]
  (try
    (let [config (config/read! project-dir)
          namespaces (parse-clojure-files! source-paths)
          analyzer-report (analyzer/analyze config namespaces)]
      (print! analyzer-report)
      (if (zero? (count analyzer-report))
        (exit! 0)
        (exit! 1)))
    (catch Exception e
      (println (ex-message e))
      (exit! 1))))
