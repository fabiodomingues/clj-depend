(ns clj-depend.internal-api
  (:require [clj-depend.analyzer :as analyzer]
            [clj-depend.config :as config]
            [clj-depend.dependency :as dependency]
            [clj-depend.parser :as parser]
            [clj-depend.snapshot :as snapshot]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn- ->project-root
  [{:keys [project-root]} context]
  (assoc context :project-root project-root))

(defn- ->config
  [{:keys [config]}
   {:keys [project-root] :as context}]
  (assoc context :config (config/resolve-config! project-root config)))

(defn- ->files
  [{:keys [files]}
   {:keys [project-root] {:keys [source-paths]} :config :as context}]
  (cond
    (not-empty files) (assoc context :files files)
    (not-empty source-paths) (assoc context :files (map #(io/file project-root %) source-paths))
    :else (assoc context :files #{project-root})))

(defn- ->namespaces
  [{:keys [namespaces]}
   {:keys [files] :as context}]
  (assoc context :namespaces namespaces)
  (let [ns-deps (parser/parse-clojure-files! files namespaces)
        dependency-graph (dependency/dependencies-graph ns-deps)]
    (assoc context :namespaces (if (empty? namespaces) (map :name ns-deps) namespaces)
                   :dependency-graph dependency-graph)))

(defn- build-context
  [options]
  (->> {}
       (->project-root options)
       (->config options)
       (->files options)
       (->namespaces options)))

(defn analyze
  [options]
  (try
    (let [context (build-context options)
          violations (analyzer/analyze context)
          _ (snapshot/dump-when-enabled! violations options)
          violations (snapshot/without-violations-present-in-snapshot-file! violations options)]
      (if (seq violations)
        {:result-code 1
         :message     (string/join "\n" (map :message violations))
         :violations  violations}
        {:result-code 0
         :message     "No violations found!"}))
    (catch Exception e
      {:result-code 2
       :message     (ex-message e)})))
