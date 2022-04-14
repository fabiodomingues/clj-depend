(ns clj-depend.util
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.namespace.parse :as namespace.parse]
            [clojure.tools.namespace.find :as namespace.find])
  (:import (java.io PushbackReader)))

(defn read-config
  [project-dir]
  (let [f (io/file project-dir ".clj-depend" "config.edn")]
    (when (.exists f)
      (with-open [r (PushbackReader. (io/reader f))]
        (edn/read r)))))

(defn parse-clojure-files
  [dirs]
  (let [ns-decls (mapcat (fn [dir]
                           (namespace.find/find-ns-decls-in-dir (io/file dir))) dirs)]
    (map (fn [ns-decl]
           {:name         (str (namespace.parse/name-from-ns-decl ns-decl))
            :dependencies (map str (namespace.parse/deps-from-ns-decl ns-decl))}) ns-decls)))
