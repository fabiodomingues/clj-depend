(ns clj-depend.parser
  (:require [clojure.tools.namespace.find :as namespace.find]
            [clojure.tools.namespace.parse :as namespace.parse]
            [clojure.java.io :as io]))

(defn parse-clojure-files!
  [source-paths namespaces]
  (let [ns-decls (mapcat (fn [source-path]
                           (namespace.find/find-ns-decls-in-dir (io/file source-path))) source-paths)]
    (keep (fn [ns-decl]
            (when (or (nil? namespaces)
                      (contains? namespaces (namespace.parse/name-from-ns-decl ns-decl)))
              {:name         (namespace.parse/name-from-ns-decl ns-decl)
               :dependencies (namespace.parse/deps-from-ns-decl ns-decl)})) ns-decls)))
