(ns clj-depend.internal-api
  (:require [clj-depend.config :as config]
            [clj-depend.parser :as parser]
            [clj-depend.analyzer :as analyzer]
            [clj-depend.dependency :as dependency]
            [clojure.string :as string]))

(defn- ->project-root
  [{:keys [project-root]} context]
  (assoc context :project-root project-root))

(defn- ->source-paths
  [{:keys [source-paths]} context]
  (assoc context :source-paths source-paths))

(defn- ->config
  [{:keys [config]} context]
  (if (map? config)
    (assoc context :config config)
    (assoc context :config (config/read! (:project-root context)))))

(defn- ->namespaces
  [{:keys [namespaces]} context]
  (assoc context :namespaces namespaces)
  (let [source-paths (:source-paths context)
        ns-deps (parser/parse-clojure-files! source-paths namespaces)
        dependency-graph (dependency/dependencies-graph ns-deps)]
    (assoc context :namespaces (if (empty? namespaces) (map :name ns-deps) namespaces)
                   :dependency-graph dependency-graph)))

(defn- violations->violations-messages
  [violations]
  (map (fn [{:keys [namespace violation]}]
         (str \" namespace \" " should not depends on " \" violation \"))
       violations))

(defn- build-context
  [options]
  (->> {}
       (->project-root options)
       (->source-paths options)
       (->config options)
       (->namespaces options)))

(defn analyze
  [options]
  (try
    (let [context (build-context options)
          violations (analyzer/analyze context)]
      (println context)
      (if (seq violations)
        {:result-code 1
         :message     (string/join "\n" (violations->violations-messages violations))
         :violations  violations}
        {:result-code 0
         :message     "No violations found!"}))
    (catch Exception e
      {:result-code 2
       :message     (ex-message e)})))
