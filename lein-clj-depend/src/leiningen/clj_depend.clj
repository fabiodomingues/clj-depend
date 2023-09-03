(ns leiningen.clj-depend
  (:refer-clojure :exclude [run!])
  (:require [clj-depend.main :as clj-depend.main]
            [leiningen.core.main :as leiningen.core]))

(defn- project->args
  [{:keys [root]} args]
  (concat (or args [])
          ["--project-root" root]))

(defn- run!
  [project & args]
  (let [result (apply clj-depend.main/run! (project->args project args))]
    (when-let [message (:message result)]
      (println message))
    (when (not= 0 (:result-code result))
      (leiningen.core/exit (:result-code result)))))

(defn clj-depend
  [project & args]
  (if leiningen.core/*info*
    (run! project args)
    (with-out-str (run! project args))))
