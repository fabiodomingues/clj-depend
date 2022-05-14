(ns ^:no-doc clj-depend.parser
  (:require [clojure.tools.namespace.find :as namespace.find]
            [clojure.tools.namespace.parse :as namespace.parse]))

(defn parse-clojure-files!
  [files namespaces]
  (let [ns-decls (mapcat (fn [file]
                           (namespace.find/find-ns-decls-in-dir file)) files)]
    (keep (fn [ns-decl]
            (when (or (nil? namespaces)
                      (contains? namespaces (namespace.parse/name-from-ns-decl ns-decl)))
              {:name         (namespace.parse/name-from-ns-decl ns-decl)
               :dependencies (namespace.parse/deps-from-ns-decl ns-decl)})) ns-decls)))
