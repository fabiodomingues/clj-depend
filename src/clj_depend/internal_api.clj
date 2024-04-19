(ns clj-depend.internal-api
  (:require [clj-depend.analyzer :as analyzer]
            [clj-depend.config :as config]
            [clj-depend.snapshot :as snapshot]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.namespace.file :as file]
            [clojure.tools.namespace.find :as namespace.find]
            [clojure.tools.namespace.parse :as namespace.parse]))

(defn ^:private ->project-root
  [{:keys [project-root]} context]
  (assoc context :project-root project-root))

(defn ^:private ->config
  [{:keys [config]}
   {:keys [project-root] :as context}]
  (assoc context :config (config/resolve-config! project-root config)))

(defn ^:private source-paths-or-project-root->files
  [{:keys [project-root] {:keys [source-paths]} :config}]
  (if (not-empty source-paths)
    (map #(io/file project-root %) source-paths)
    #{project-root}))

(defn ^:private analyze?
  [{:keys [file namespace]} files-to-be-analyzed namespaces-to-be-analyzed]
  (boolean (cond
             (and (not-empty files-to-be-analyzed) (not-empty namespaces-to-be-analyzed))
             (and (some #(.startsWith (.toPath file) (.toPath %)) files-to-be-analyzed)
                  (contains? namespaces-to-be-analyzed namespace))

             (not-empty files-to-be-analyzed)
             (some #(.startsWith (.toPath file) (.toPath %)) files-to-be-analyzed)

             (not-empty namespaces-to-be-analyzed)
             (contains? namespaces-to-be-analyzed namespace)

             :else
             true)))

(defn ^:private ->namespaces-and-dependencies
  [_options
   context]
  (let [files (source-paths-or-project-root->files context)
        clojure-files (mapcat #(namespace.find/find-sources-in-dir %) files)]
    (assoc context :namespaces-and-dependencies (keep (fn [file]
                                                        (when-let [ns-decl (file/read-file-ns-decl file)]
                                                          {:namespace    (namespace.parse/name-from-ns-decl ns-decl)
                                                           :dependencies (namespace.parse/deps-from-ns-decl ns-decl)
                                                           :file         file})) clojure-files))))

(defn ^:private ->namespaces-to-be-analyzed
  [{files-to-be-analyzed      :files
    namespaces-to-be-analyzed :namespaces}
   {:keys [namespaces-and-dependencies] :as context}]
  (assoc context :namespaces-to-be-analyzed (->> namespaces-and-dependencies
                                                 (filter #(analyze? % files-to-be-analyzed namespaces-to-be-analyzed))
                                                 (map :namespace))))

(defn ^:private build-context
  [options]
  (->> {}
       (->project-root options)
       (->config options)
       (->namespaces-and-dependencies options)
       (->namespaces-to-be-analyzed options)))

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
