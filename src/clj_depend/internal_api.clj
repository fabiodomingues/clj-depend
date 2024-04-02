(ns clj-depend.internal-api
  (:require [clj-depend.analyzer :as analyzer]
            [clj-depend.config :as config]
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

(defn ^:private file-within-some-source-paths?
  [file source-paths]
  (some #(.startsWith (.toPath file) (.toPath %)) source-paths))

(defn ^:private files-within-source-paths
  [files source-paths]
  (filter #(file-within-some-source-paths? % source-paths) files))

(defn- ->files
  [{:keys [files]}
   {:keys [project-root] {:keys [source-paths]} :config :as context}]
  (let [source-paths (map #(io/file project-root %) source-paths)]
    (cond
      (not-empty files) (assoc context :files (files-within-source-paths files source-paths))
      (not-empty source-paths) (assoc context :files source-paths)
      :else (assoc context :files #{project-root}))))

(defn- ->namespaces
  [{:keys [namespaces]}
   {:keys [files] :as context}]
  (let [ns-deps (parser/parse-clojure-files! files namespaces)]
    (assoc context :dependencies-by-namespace (reduce-kv (fn [m k v] (assoc m k (:dependencies (first v))))
                                                         {}
                                                         (group-by :name ns-deps)))))

(defn- build-context
  [options]
  (->> {}
       (->project-root options)
       (->config options)
       (->files options)
       (->namespaces options)))

(defn configured?
  [project-root]
  (config/configured? project-root))

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
